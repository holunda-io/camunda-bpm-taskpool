package io.holunda.polyflow.view.jpa

import com.fasterxml.jackson.databind.ObjectMapper
import io.holixon.axon.gateway.configuration.query.EnableRevisionAwareQueryGateway
import io.holixon.axon.gateway.query.QueryResponseMessageResponseType
import io.holixon.axon.gateway.query.RevisionValue
import io.holunda.camunda.taskpool.api.business.AuthorizationChange.Companion.addGroup
import io.holunda.camunda.taskpool.api.business.AuthorizationChange.Companion.addUser
import io.holunda.camunda.taskpool.api.business.DataEntryCreatedEvent
import io.holunda.camunda.taskpool.api.business.DataEntryUpdatedEvent
import io.holunda.camunda.taskpool.api.business.Modification
import io.holunda.camunda.taskpool.api.business.ProcessingType
import io.holunda.camunda.variable.serializer.serialize
import io.holunda.polyflow.view.auth.User
import io.holunda.polyflow.view.jpa.itest.TestApplication
import io.holunda.polyflow.view.query.data.DataEntriesForUserQuery
import io.holunda.polyflow.view.query.data.DataEntriesQueryResult
import mu.KLogging
import org.assertj.core.api.Assertions.assertThat
import org.axonframework.messaging.GenericMessage
import org.axonframework.queryhandling.QueryGateway
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.transaction.Transactional

@RunWith(SpringRunner::class)
@SpringBootTest(
  classes = [TestApplication::class],
  properties = [
    "axon-gateway.query.type=revision-aware",
    "axon-gateway.query.revision-aware.default-query-timeout=10"
  ]
)
@Transactional
@ActiveProfiles("itest")
@EnableRevisionAwareQueryGateway
internal class JpaPolyflowViewServiceDataEntryRevisionSupportITest {

  companion object : KLogging()

  lateinit var executorService: ExecutorService

  @Autowired
  lateinit var dbCleaner: DbCleaner

  @Autowired
  lateinit var queryGateway: QueryGateway

  @Autowired
  lateinit var jpaPolyflowViewService: JpaPolyFlowJpaServiceTxFacade

  @Autowired
  lateinit var objectMapper: ObjectMapper

  private val id = UUID.randomUUID().toString()
  private val now = Instant.now()
  private val payload = mapOf(
    "key" to "value",
    "key-int" to 1,
    "complex" to Pojo(
      attribute1 = "value",
      attribute2 = Date.from(now)
    )
  )

  @Before
  fun `ingest events`() {

    executorService = Executors.newFixedThreadPool(2)

    jpaPolyflowViewService.on(
      event = DataEntryCreatedEvent(
        entryType = "io.polyflow.test",
        entryId = id,
        type = "Test",
        applicationName = "test-application",
        name = "Test Entry 1",
        state = ProcessingType.IN_PROGRESS.of("In progress"),
        payload = serialize(payload = payload, mapper = objectMapper),
        authorizations = listOf(
          addUser("kermit"),
          addGroup("muppets")
        ),
        createModification = Modification(
          time = OffsetDateTime.ofInstant(now, ZoneOffset.UTC),
          username = "kermit",
          log = "Created",
          logNotes = "Created the entry"
        )
      ),
      metaData = RevisionValue(revision = 1).toMetaData()
    )

    send_event_update_with_revision(2)
  }

  @After
  fun `cleanup projection`() {
    dbCleaner.cleanup()
    executorService.shutdown()
  }

  @Test
  fun `should find the entry filter by receiving the event after the query`() {

    val user = User("kermit", setOf("muppets"))
    val filters = listOf("key-int=1")
    val query = DataEntriesForUserQuery(user = user, filters = filters)
    val revisionValue = RevisionValue(revision = 3)

    val subscription = queryGateway.query(
      GenericMessage.asMessage(query).andMetaData(revisionValue.toMetaData()),
      QueryResponseMessageResponseType.queryResponseMessageResponseType<DataEntriesQueryResult>()
    )

    // subscription in one thread
    val queryResult = executorService.submit(Callable {
      subscription.handle { dataEntriesQueryResult, throwable ->
        if (throwable == null) {
          dataEntriesQueryResult.elements
        } else {
          logger.warn { "Requested entries for user $user with filter $filters and rev. $revisionValue were not found" }
          emptyList()
        }
      }.join()
    })

    // wait a second
    Thread.sleep(2000)

    // submission of the event in another thread
    executorService.submit {
      send_event_update_with_revision(3)
    }

    val result = queryResult.get()
    assertThat(result).isNotEmpty
    assertThat(result[0].name).isEqualTo("Test Entry 3")

  }

  @Test
  fun `should not find the entry filter`() {

    val user = User("kermit", setOf("muppets"))
    val filters = listOf("key-int=1")
    val query = DataEntriesForUserQuery(user = user, filters = filters)
    val revisionValue = RevisionValue(revision = 3)

    val subscription = queryGateway.query(
      GenericMessage.asMessage(query).andMetaData(revisionValue.toMetaData()),
      QueryResponseMessageResponseType.queryResponseMessageResponseType<DataEntriesQueryResult>()
    )

    // subscription in one thread
    val queryResult = executorService.submit(Callable {
      subscription.handle { dataEntriesQueryResult, throwable ->
        if (throwable == null) {
          dataEntriesQueryResult.elements
        } else {
          logger.warn { "Requested entries for user $user with filter $filters and rev. $revisionValue were not found" }
          emptyList()
        }
      }.join()
    })

    val result = queryResult.get()
    assertThat(result).isEmpty()

  }


  fun send_event_update_with_revision(revision: Long) {
    jpaPolyflowViewService.on(
      event = DataEntryUpdatedEvent(
        entryType = "io.polyflow.test",
        entryId = id,
        type = "Test",
        applicationName = "test-application",
        name = "Test Entry $revision",
        state = ProcessingType.IN_PROGRESS.of("In review"),
        payload = serialize(payload = payload, mapper = objectMapper),
        authorizations = listOf(),
        updateModification = Modification(
          time = OffsetDateTime.ofInstant(now, ZoneOffset.UTC).plus(10 + revision, ChronoUnit.SECONDS),
          username = "ironman",
          log = "Updated",
          logNotes = "Updated the entry"
        )
      ),
      metaData = RevisionValue(revision = revision).toMetaData()
    )
  }
}
