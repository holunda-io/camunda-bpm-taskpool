package io.holunda.polyflow.view.simple.service

import io.holixon.axon.gateway.query.RevisionValue
import io.holunda.camunda.taskpool.api.business.*
import io.holunda.camunda.taskpool.api.business.AuthorizationChange.Companion.addGroup
import io.holunda.camunda.taskpool.api.business.AuthorizationChange.Companion.addUser
import io.holunda.polyflow.view.DataEntry
import io.holunda.polyflow.view.auth.User
import io.holunda.polyflow.view.query.data.DataEntriesForUserQuery
import io.holunda.polyflow.view.query.data.DataEntriesQuery
import io.holunda.polyflow.view.query.data.DataEntriesQueryResult
import io.holunda.polyflow.view.query.data.DataEntryForIdentityQuery
import org.assertj.core.api.Assertions.assertThat
import org.axonframework.messaging.MetaData
import org.axonframework.queryhandling.QueryResponseMessage
import org.axonframework.queryhandling.SimpleQueryUpdateEmitter
import org.camunda.bpm.engine.variable.Variables
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.*

class SimpleDataEntryServiceTest {

  private var service: SimpleDataEntryService = SimpleDataEntryService(queryUpdateEmitter = SimpleQueryUpdateEmitter.builder().build())
  private val id = UUID.randomUUID().toString()
  private val id2 = UUID.randomUUID().toString()
  private val id3 = UUID.randomUUID().toString()
  private val id4 = UUID.randomUUID().toString()
  private val now = Instant.now()

  @BeforeEach
  fun `ingest events`() {
    val payload = Variables.fromMap(mapOf("key" to "value", "key-int" to 1, "complex" to mapOf("attribute1" to "value", "attribute2" to Date.from(now))))

    service.on(
      event = DataEntryCreatedEvent(
        entryType = "io.polyflow.test",
        entryId = id,
        type = "Test",
        applicationName = "test-application",
        name = "Test Entry 1",
        state = ProcessingType.IN_PROGRESS.of("In progress"),
        payload = payload,
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

    service.on(
      event = DataEntryUpdatedEvent(
        entryType = "io.polyflow.test",
        entryId = id,
        type = "Test",
        applicationName = "test-application",
        name = "Test Entry 1",
        state = ProcessingType.IN_PROGRESS.of("In review"),
        payload = payload,
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

    service.on(
      event = DataEntryCreatedEvent(
        entryType = "io.polyflow.test",
        entryId = id2,
        type = "Test",
        applicationName = "test-application",
        name = "Test Entry 2",
        state = ProcessingType.IN_PROGRESS.of("In review"),
        payload = Variables.putValue("key-int", 2).putValue("key", "value"),
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

    service.on(
      event = DataEntryCreatedEvent(
        entryType = "io.polyflow.test",
        entryId = id3,
        type = "Test of deleted",
        applicationName = "test-application",
        name = "Test Entry 3",
        state = ProcessingType.IN_PROGRESS.of("In review"),
        payload = Variables.putValue("key-int", 3).putValue("key", "value"),
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

    service.on(
      event = DataEntryDeletedEvent(
        entryType = "io.polyflow.test",
        entryId = id3,
        deleteModification = Modification(
          time = OffsetDateTime.ofInstant(now, ZoneOffset.UTC),
          log = "Deleted",
          logNotes = "Created by mistake"
        ),
        state = ProcessingType.DELETED.of("Create error")
      ),
      metaData = MetaData.emptyInstance()
    )

    service.on(
      event = DataEntryCreatedEvent(
        entryType = "io.polyflow.test",
        entryId = id4,
        type = "Test sort",
        applicationName = "test-application",
        name = "Test Entry 4",
        state = ProcessingType.IN_PROGRESS.of("In review"),
        payload = Variables.putValue("key-int", 4).putValue("key", "other-value"),
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
        correlations = Variables.createVariables().addCorrelation("io.polyflow.test", id2)
      ),
      metaData = MetaData.emptyInstance()
    )
  }

  @AfterEach
  fun `cleanup projection`() {
    listOf(id, id2, id3, id4).forEach {
      service.on(DataEntryDeletedEvent(entryId = "io.holunda.polyflow.test", entryType = it), metaData = MetaData.emptyInstance())
    }
  }


  @Test
  fun `should find the entry by id`() {
    val result = service.query(
      DataEntryForIdentityQuery(entryType = "io.polyflow.test", entryId = id)
    )
    assertThat(result.payload).isNotNull
    assertTestDataEntry1(result.payload)
  }

  @Test
  fun `should find the entry by user`() {

    val result = service.query(
      DataEntriesForUserQuery(user = User("kermit", groups = setOf()))
    )

    assertResultIsTestEntry1(result)

    assertResultIsTestEntry1And2(
      service.query(
        DataEntriesForUserQuery(user = User("superman", groups = setOf("muppets")))
      )
    )

    assertResultIsTestEntry1(
      service.query(
        DataEntriesForUserQuery(user = User("ironman", groups = setOf()))
      )
    )

    assertThat(
      service.query(
        DataEntriesForUserQuery(user = User("superman", groups = setOf("avengers")))
      ).payload.elements
    ).isEmpty()

  }

  @Test
  fun `should not fail deleted already deleted entry`() {
    service.on(
      event = DataEntryDeletedEvent(
        entryType = "io.polyflow.test",
        entryId = id3,
        deleteModification = Modification(
          time = OffsetDateTime.ofInstant(now, ZoneOffset.UTC),
          log = "Deleted",
          logNotes = "Created by mistake"
        ),
        state = ProcessingType.DELETED.of("Create error")
      ),
      metaData = MetaData.emptyInstance()
    )
  }

  @Test
  fun `should find the entry by user and filter`() {
    val query = DataEntriesForUserQuery(user = User("kermit", groups = setOf("muppets")), filters = listOf("key-int=1"))
    assertResultIsTestEntry1(
      service.query(
        query
      )
    )
  }

  @Test
  fun `should not find the deleted entry`() {

    val result = service.query(
      DataEntryForIdentityQuery(entryType = "io.polyflow.test", entryId = id3)
    )
    assertThat(result).isNotNull
    assertThat(result.payload).isNull()
  }

  @Test
  fun `should sort entries with multiple criteria`() {

    val result = service.query(
      DataEntriesQuery(sort = listOf("+type", "-name"))
    )
    assertThat(result.payload.elements.map { it.entryId }).containsExactlyInAnyOrder(id2, id, id4)
  }

  @Suppress("DEPRECATION")
  @Test
  fun `sort should be backwards compatible`() {

    val result = service.query(
      DataEntriesQuery(sort = "+type")
    )
    assertThat(result.payload.elements.map { it.entryId }).containsExactlyInAnyOrder(id, id2, id4)
  }

  @Test
  fun `should not find data entry with correlations`() {
    val result = service.query(
      DataEntriesQuery(filters = listOf("key-int=2")) // key-int 2 is an attribute of data entry 2
    )

    assertThat(result.payload.elements.map { it.entryId }).containsExactlyInAnyOrder(id2) // id4 is not found by correlation to id2, due to property
  }

  @Test
  fun `should find data entry by involvements`() {
    val result = service.query(
      DataEntriesForUserQuery(user = User("kermit", mutableSetOf("muppets")), involvementsOnly = true)
    )

    assertThat(result.payload.elements.map { it.entryId }).containsExactlyInAnyOrder(id) // user is allowed to see two dataEntries but has only one involvement
  }

  private fun assertResultIsTestEntry1(result: QueryResponseMessage<DataEntriesQueryResult>) {
    assertThat(result.payload.elements.size).isEqualTo(1)
    val dataEntry = result.payload.elements[0]
    assertTestDataEntry1(dataEntry)
  }

  private fun assertResultIsTestEntry1And2(result: QueryResponseMessage<DataEntriesQueryResult>) {
    assertThat(result.payload.elements.size).isEqualTo(2)
    assertThat(result.payload.elements.map { it.entryId }).containsExactlyInAnyOrder(id, id2)
    assertTestDataEntry1(result.payload.elements.first { it.entryId == id })
    assertTestDataEntry2(result.payload.elements.first { it.entryId == id2 })
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


}
