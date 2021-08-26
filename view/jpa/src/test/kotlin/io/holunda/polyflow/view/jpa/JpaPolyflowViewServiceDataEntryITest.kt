package io.holunda.polyflow.view.jpa

import com.fasterxml.jackson.databind.ObjectMapper
import io.holixon.axon.gateway.query.RevisionValue
import io.holunda.camunda.taskpool.api.business.AuthorizationChange.Companion.addGroup
import io.holunda.camunda.taskpool.api.business.AuthorizationChange.Companion.addUser
import io.holunda.camunda.taskpool.api.business.DataEntryCreatedEvent
import io.holunda.camunda.taskpool.api.business.DataEntryUpdatedEvent
import io.holunda.camunda.taskpool.api.business.Modification
import io.holunda.camunda.taskpool.api.business.ProcessingType
import io.holunda.camunda.variable.serializer.serialize
import io.holunda.polyflow.view.DataEntry
import io.holunda.polyflow.view.auth.User
import io.holunda.polyflow.view.jpa.itest.TestApplication
import io.holunda.polyflow.view.query.data.DataEntriesForUserQuery
import io.holunda.polyflow.view.query.data.DataEntriesQuery
import io.holunda.polyflow.view.query.data.DataEntriesQueryResult
import io.holunda.polyflow.view.query.data.DataEntryForIdentityQuery
import org.assertj.core.api.Assertions.assertThat
import org.axonframework.messaging.MetaData
import org.axonframework.queryhandling.GenericSubscriptionQueryUpdateMessage
import org.axonframework.queryhandling.QueryResponseMessage
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.axonframework.queryhandling.SubscriptionQueryUpdateMessage
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.atLeast
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.function.Predicate

@RunWith(SpringRunner::class)
@SpringBootTest(
  classes = [TestApplication::class], properties = [
    "polyflow.view.jpa.event-emitting-type=direct"
  ]
)
@ActiveProfiles("itest", "mock-query-emitter")
@Transactional
@DirtiesContext
internal class JpaPolyflowViewServiceDataEntryITest {

  val emittedQueryUpdates: MutableList<QueryUpdate<Any>> = mutableListOf()

  @Autowired
  lateinit var queryUpdateEmitter: QueryUpdateEmitter

  @Autowired
  lateinit var jpaPolyflowViewService: JpaPolyflowViewService

  @Autowired
  lateinit var dbCleaner: DbCleaner

  @Autowired
  lateinit var objectMapper: ObjectMapper

  private val id = UUID.randomUUID().toString()
  private val id2 = UUID.randomUUID().toString()
  private val now = Instant.now()

  @Before
  fun `ingest events`() {
    val payload = mapOf(
      "key" to "value",
      "key-int" to 1,
      "complex" to Pojo(
        attribute1 = "value",
        attribute2 = Date.from(now)
      )
    )

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

    jpaPolyflowViewService.on(
      event = DataEntryUpdatedEvent(
        entryType = "io.polyflow.test",
        entryId = id,
        type = "Test",
        applicationName = "test-application",
        name = "Test Entry 1",
        state = ProcessingType.IN_PROGRESS.of("In review"),
        payload = serialize(payload = payload, mapper = objectMapper),
        authorizations = listOf(
          addUser("ironman"),
        ),
        updateModification = Modification(
          time = OffsetDateTime.ofInstant(now, ZoneOffset.UTC).plus(10, ChronoUnit.SECONDS),
          username = "ironman",
          log = "Updated",
          logNotes = "Updated the entry"
        )
      ),
      metaData = RevisionValue(revision = 2).toMetaData()
    )

    jpaPolyflowViewService.on(
      event = DataEntryCreatedEvent(
        entryType = "io.polyflow.test",
        entryId = id2,
        type = "Test",
        applicationName = "test-application",
        name = "Test Entry 2",
        state = ProcessingType.IN_PROGRESS.of("In review"),
        payload = serialize(payload = mapOf("key-int" to 2, "key" to "value"), mapper = objectMapper),
        authorizations = listOf(
          addUser("piggy"),
          addGroup("muppets")
        ),
        createModification = Modification(
          time = OffsetDateTime.ofInstant(now, ZoneOffset.UTC),
          username = "piggy",
          log = "Created",
          logNotes = "Created the entry"
        )
      ),
      metaData = MetaData.emptyInstance()
    )
  }

  @After
  fun `cleanup projection`() {
    dbCleaner.cleanup()
    // clear updates
    emittedQueryUpdates.clear()
  }

  @Test
  fun `should find the entry by id`() {
    assertResultIsTestEntry1(
      jpaPolyflowViewService.query(
        DataEntryForIdentityQuery(entryType = "io.polyflow.test", entryId = id)
      )
    )
  }

  @Test
  fun `should find the entry by user`() {

    val result = jpaPolyflowViewService.query(
      DataEntriesForUserQuery(user = User("kermit", groups = setOf()))
    )

    assertResultIsTestEntry1(result)

    assertResultIsTestEntry1And2(
      jpaPolyflowViewService.query(
        DataEntriesForUserQuery(user = User("superman", groups = setOf("muppets")))
      )
    )

    assertResultIsTestEntry1(
      jpaPolyflowViewService.query(
        DataEntriesForUserQuery(user = User("ironman", groups = setOf()))
      )
    )

    assertThat(
      jpaPolyflowViewService.query(
        DataEntriesForUserQuery(user = User("superman", groups = setOf("avengers")))
      ).payload.elements
    ).isEmpty()

  }

  @Test
  fun `should find the entry by user and filter`() {
    val query = DataEntriesForUserQuery(user = User("kermit", groups = setOf("muppets")), filters = listOf("key-int=1"))
    assertResultIsTestEntry1(
      jpaPolyflowViewService.query(
        query
      )
    )

    query_updates_have_been_emitted(query, id, 2)
    query_updates_have_been_emitted(query, id2, 0)
  }

  @Test
  fun `should find the entry by filter`() {
    assertResultIsTestEntry1And2(
      jpaPolyflowViewService.query(
        DataEntriesQuery(filters = listOf("key=value"))
      )
    )
  }


  private fun <T : Any> query_updates_have_been_emitted(query: T, id: String, revision: Long) {
    captureEmittedQueryUpdates()

    val updates = emittedQueryUpdates
      .filter { it.queryType == query::class.java }
      .filter { it.predicate.test(query) }
      .map { it.update as GenericSubscriptionQueryUpdateMessage<*> }
      .map { message -> (message.payload as DataEntriesQueryResult).elements.map { entry -> entry.entryId } to RevisionValue.fromMetaData(message.metaData).revision }

    assertThat(updates)
      .`as`("Query updates for query $query")
      .containsAnyElementsOf(listOf(listOf(id) to revision))
  }


  private fun assertResultIsTestEntry1(result: QueryResponseMessage<DataEntriesQueryResult>) {
    assertThat(result.payload.elements.size).isEqualTo(1)
    val dataEntry = result.payload.elements[0]
    assertTestDataEntry1(dataEntry)
  }

  private fun assertResultIsTestEntry1And2(result: QueryResponseMessage<DataEntriesQueryResult>) {
    assertThat(result.payload.elements.size).isEqualTo(2)
    assertTestDataEntry1(result.payload.elements[0])
    assertTestDataEntry2(result.payload.elements[1])
  }


  private fun assertTestDataEntry1(dataEntry: DataEntry) {
    assertThat(dataEntry.entryId).isEqualTo(id)
    assertThat(dataEntry.entryType).isEqualTo("io.polyflow.test")
    assertThat(dataEntry.name).isEqualTo("Test Entry 1")
    assertThat(dataEntry.payload).containsKeys("key", "key-int", "complex")
    assertThat(dataEntry.protocol.size).isEqualTo(2)
    assertThat(dataEntry.protocol[0].time).isEqualTo(now)
    assertThat(dataEntry.protocol[0].username).isEqualTo("kermit")
    assertThat(dataEntry.protocol[1].time).isEqualTo(now.plus(10, ChronoUnit.SECONDS))
    assertThat(dataEntry.protocol[1].username).isEqualTo("ironman")
  }

  private fun assertTestDataEntry2(dataEntry: DataEntry) {
    assertThat(dataEntry.entryId).isEqualTo(id2)
    assertThat(dataEntry.entryType).isEqualTo("io.polyflow.test")
    assertThat(dataEntry.name).isEqualTo("Test Entry 2")
    assertThat(dataEntry.payload).containsKeys("key-int")
    assertThat(dataEntry.protocol.size).isEqualTo(1)
    assertThat(dataEntry.protocol[0].time).isEqualTo(now)
    assertThat(dataEntry.protocol[0].username).isEqualTo("piggy")
  }

  private fun captureEmittedQueryUpdates(): List<QueryUpdate<Any>> {
    val queryTypeCaptor = argumentCaptor<Class<Any>>()
    val predicateCaptor = argumentCaptor<Predicate<Any>>()
    val updateCaptor = argumentCaptor<SubscriptionQueryUpdateMessage<Any>>()
    verify(queryUpdateEmitter, atLeast(0)).emit(queryTypeCaptor.capture(), predicateCaptor.capture(), updateCaptor.capture())
    clearInvocations(queryUpdateEmitter)

    val foundUpdates = queryTypeCaptor.allValues
      .zip(predicateCaptor.allValues)
      .zip(updateCaptor.allValues) { (queryType, predicate), update ->
        QueryUpdate(queryType, predicate, update)
      }

    emittedQueryUpdates.addAll(foundUpdates)
    return foundUpdates
  }

  data class QueryUpdate<E>(val queryType: Class<E>, val predicate: Predicate<E>, val update: Any)

}
