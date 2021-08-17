package io.holunda.polyflow.view.jpa

import com.fasterxml.jackson.databind.ObjectMapper
import io.holunda.camunda.taskpool.api.business.AuthorizationChange.Companion.addGroup
import io.holunda.camunda.taskpool.api.business.AuthorizationChange.Companion.addUser
import io.holunda.camunda.taskpool.api.business.DataEntryCreatedEvent
import io.holunda.camunda.taskpool.api.business.DataEntryUpdatedEvent
import io.holunda.camunda.taskpool.api.business.Modification
import io.holunda.camunda.taskpool.api.business.ProcessingType
import io.holunda.camunda.variable.serializer.serialize
import io.holunda.polyflow.view.DataEntry
import io.holunda.polyflow.view.auth.User
import io.holunda.polyflow.view.query.data.DataEntriesForUserQuery
import io.holunda.polyflow.view.query.data.DataEntriesQuery
import io.holunda.polyflow.view.query.data.DataEntriesQueryResult
import io.holunda.polyflow.view.query.data.DataEntryForIdentityQuery
import org.assertj.core.api.Assertions.assertThat
import org.axonframework.messaging.MetaData
import org.axonframework.queryhandling.QueryResponseMessage
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.*

@RunWith(SpringRunner::class)
@SpringBootTest
@ActiveProfiles("itest")
internal class JpaPolyflowViewServiceITest {

  @MockBean
  lateinit var queryUpdateEmitter: QueryUpdateEmitter

  @Autowired
  lateinit var jpaPolyflowViewService: JpaPolyflowViewService

  @Autowired
  lateinit var objectMapper: ObjectMapper

  private val id = UUID.randomUUID().toString()
  private val id2 = UUID.randomUUID().toString()
  private val now = Instant.now()

  @Before
  internal fun `ingest events`() {

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
      metaData = MetaData.emptyInstance()
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
      metaData = MetaData.emptyInstance()
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
  internal fun `cleanup projection`() {
    jpaPolyflowViewService.dataEntryRepository.deleteAll()
  }

  @Test
  internal fun `should find the entry by id`() {
    assertResultIsTestEntry1(
      jpaPolyflowViewService.query(
        DataEntryForIdentityQuery(entryType = "io.polyflow.test", entryId = id)
      )
    )
  }

  @Test
  internal fun `should find the entry by user`() {

    assertResultIsTestEntry1(
      jpaPolyflowViewService.query(
        DataEntriesForUserQuery(user = User("kermit", groups = setOf()))
      )
    )

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
  internal fun `should find the entry by user and filter`() {
    assertResultIsTestEntry1(
      jpaPolyflowViewService.query(
        DataEntriesForUserQuery(user = User("kermit", groups = setOf("muppets")), filters = listOf("key-int=1"))
      )
    )
  }

  @Test
  internal fun `should find the entry by filter`() {
    assertResultIsTestEntry1And2(
      jpaPolyflowViewService.query(
        DataEntriesQuery(filters = listOf("key=value"))
      )
    )
  }


  internal fun assertResultIsTestEntry1(result: QueryResponseMessage<DataEntriesQueryResult>) {
    assertThat(result.payload.elements.size).isEqualTo(1)
    val dataEntry = result.payload.elements[0]
    assertTestDataEntry1(dataEntry)
  }

  internal fun assertResultIsTestEntry1And2(result: QueryResponseMessage<DataEntriesQueryResult>) {
    assertThat(result.payload.elements.size).isEqualTo(2)
    assertTestDataEntry1(result.payload.elements[0])
    assertTestDataEntry2(result.payload.elements[1])
  }


  internal fun assertTestDataEntry1(dataEntry: DataEntry) {
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

  internal fun assertTestDataEntry2(dataEntry: DataEntry) {
    assertThat(dataEntry.entryId).isEqualTo(id2)
    assertThat(dataEntry.entryType).isEqualTo("io.polyflow.test")
    assertThat(dataEntry.name).isEqualTo("Test Entry 2")
    assertThat(dataEntry.payload).containsKeys("key-int")
    assertThat(dataEntry.protocol.size).isEqualTo(1)
    assertThat(dataEntry.protocol[0].time).isEqualTo(now)
    assertThat(dataEntry.protocol[0].username).isEqualTo("piggy")
  }

}
