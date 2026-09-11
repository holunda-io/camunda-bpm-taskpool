package io.holunda.polyflow.view.jpa

import com.fasterxml.jackson.databind.ObjectMapper
import io.holunda.camunda.taskpool.api.business.AuthorizationChange.Companion.addGroup
import io.holunda.camunda.taskpool.api.business.AuthorizationChange.Companion.addUser
import io.holunda.camunda.taskpool.api.business.DataEntryCreatedEvent
import io.holunda.camunda.taskpool.api.business.Modification
import io.holunda.camunda.taskpool.api.business.ProcessingType
import io.holunda.camunda.taskpool.api.business.addCorrelation
import io.holunda.camunda.variable.serializer.serialize
import io.holunda.polyflow.view.jpa.itest.TestApplication
import io.holunda.polyflow.view.query.data.DataEntriesQuery
import org.assertj.core.api.Assertions.assertThat
import org.axonframework.messaging.MetaData
import org.camunda.bpm.engine.variable.Variables
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*
import java.util.function.Predicate

@SpringBootTest(
  classes = [TestApplication::class],
  properties = [
    "polyflow.view.jpa.stored-items=data-entry",
    "polyflow.view.jpa.include-correlated-data-entries-in-data-entry-queries=true"
  ]
)
@ActiveProfiles("itest-tc-mariadb", "mock-query-emitter")
@Transactional
@DirtiesContext
internal class JpaPolyflowViewServiceDataEntryCorrelationITest {

  private val emittedQueryUpdates: MutableList<QueryUpdate<Any>> = mutableListOf()

  @Autowired
  lateinit var jpaPolyflowViewService: JpaPolyflowViewDataEntryService

  @Autowired
  lateinit var dbCleaner: DbCleaner

  @Autowired
  lateinit var objectMapper: ObjectMapper

  private val id1 = UUID.randomUUID().toString()
  private val id2 = UUID.randomUUID().toString()
  private val now = Instant.now()

  @BeforeEach
  fun `ingest events`() {


    jpaPolyflowViewService.on(
      event = DataEntryCreatedEvent(
        entryType = "io.polyflow.test",
        entryId = id1,
        type = "Test",
        applicationName = "test-application",
        name = "Test Entry 2",
        state = ProcessingType.IN_PROGRESS.of("In review"),
        payload = serialize(payload = mapOf("key-int" to 1, "key" to "value"), mapper = objectMapper),
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
      metaData = MetaData.emptyInstance(),
      eventTimestamp = now
    )


    jpaPolyflowViewService.on(
      event = DataEntryCreatedEvent(
        entryType = "io.polyflow.test",
        entryId = id2,
        type = "Test sort",
        applicationName = "test-application",
        name = "Test Entry 4",
        state = ProcessingType.IN_PROGRESS.of("In review"),
        payload = serialize(payload = mapOf("key-int" to 2, "key" to "other-value"), mapper = objectMapper),
        authorizations = listOf(
          addUser("hulk"),
          addGroup("avenger")
        ),
        createModification = Modification(
          time = OffsetDateTime.ofInstant(now, ZoneOffset.UTC),
          username = "piggy",
          log = "Created",
          logNotes = "Created the entry"
        ),
        correlations = Variables.createVariables().addCorrelation("io.polyflow.test", id1)
      ),
      metaData = MetaData.emptyInstance(),
      eventTimestamp = now
    )
  }

  @AfterEach
  fun `cleanup projection`() {
    dbCleaner.cleanup()
    // clear updates
    emittedQueryUpdates.clear()
  }

  @Test
  fun `should find data entry with correlations`() {
    val result = jpaPolyflowViewService.query(
      DataEntriesQuery(filters = listOf("key-int=1")) // key-int 1 is an attribute of data entry 1
    )

    assertThat(result.payload.elements.map { it.entryId }).containsExactlyInAnyOrder(id2, id1) // id2 is found by correlation to id1, due to property
  }

  data class QueryUpdate<E>(val queryType: Class<E>, val predicate: Predicate<E>, val update: Any)

}
