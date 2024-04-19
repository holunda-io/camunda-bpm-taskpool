package io.holunda.polyflow.datapool.core.business

import com.fasterxml.jackson.databind.ObjectMapper
import io.holunda.camunda.taskpool.api.business.*
import io.holunda.polyflow.bus.jackson.JsonAutoDetectAnyVisibility
import io.holunda.polyflow.bus.jackson.configurePolyflowJacksonObjectMapper
import io.holunda.polyflow.datapool.core.DeletionStrategy
import io.holunda.polyflow.datapool.core.configurePolyflowJacksonObjectMapperForDatapool
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.axonframework.eventsourcing.AggregateDeletedException
import org.axonframework.test.aggregate.AggregateTestFixture
import org.camunda.bpm.engine.variable.Variables
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import java.util.*

class DataEntryAggregateTest {

  private val fixture = AggregateTestFixture(DataEntryAggregate::class.java)

  companion object {
    val LAX_POLICY = object : DeletionStrategy {
      override fun strictMode(): Boolean = false
    }
    val STRICT_POLICY = object : DeletionStrategy {
      override fun strictMode(): Boolean = true
    }
  }

  @Test
  fun `should create aggregate`() {

    val command = CreateDataEntryCommand(
      dataEntryChange = DataEntryChange(
        entryType = "io.holunda.My",
        entryId = UUID.randomUUID().toString(),
        applicationName = "myApp",
        name = "My Entry 4711",
        type = "My",
        payload = Variables.createVariables(),
        correlations = newCorrelations()
      )
    )

    fixture
      .givenNoPriorActivity()
      .`when`(command)
      .expectEvents(
        DataEntryCreatedEvent(
          entryId = command.dataEntryChange.entryId,
          entryType = command.dataEntryChange.entryType,
          name = command.dataEntryChange.name,
          type = command.dataEntryChange.type,
          applicationName = command.dataEntryChange.applicationName,
          state = command.dataEntryChange.state,
          description = command.dataEntryChange.description,
          payload = command.dataEntryChange.payload,
          correlations = command.dataEntryChange.correlations,
          createModification = command.dataEntryChange.modification,
          authorizations = command.dataEntryChange.authorizationChanges,
          formKey = command.dataEntryChange.formKey
        )
      )
  }

  @Test
  fun `should update aggregate`() {

    val command = UpdateDataEntryCommand(
      dataEntryChange = DataEntryChange(
        entryType = "io.holunda.My",
        entryId = UUID.randomUUID().toString(),
        applicationName = "myApp",
        name = "My Entry 4711",
        type = "My",
        payload = Variables.createVariables(),
        correlations = newCorrelations()
      )
    )
    fixture
      .registerInjectableResource(LAX_POLICY)
      .given(
        DataEntryCreatedEvent(
          entryId = command.dataEntryChange.entryId,
          entryType = command.dataEntryChange.entryType,
          name = "Some name",
          type = "Another",
          applicationName = "Different application",
          state = command.dataEntryChange.state,
          description = command.dataEntryChange.description,
          payload = command.dataEntryChange.payload,
          correlations = command.dataEntryChange.correlations,
          createModification = command.dataEntryChange.modification,
          authorizations = command.dataEntryChange.authorizationChanges,
          formKey = command.dataEntryChange.formKey
        )
      )
      .`when`(command)
      .expectEvents(
        DataEntryUpdatedEvent(
          entryId = command.dataEntryChange.entryId,
          entryType = command.dataEntryChange.entryType,
          name = command.dataEntryChange.name,
          type = command.dataEntryChange.type,
          applicationName = command.dataEntryChange.applicationName,
          state = command.dataEntryChange.state,
          description = command.dataEntryChange.description,
          payload = command.dataEntryChange.payload,
          correlations = command.dataEntryChange.correlations,
          updateModification = command.dataEntryChange.modification,
          authorizations = command.dataEntryChange.authorizationChanges,
          formKey = command.dataEntryChange.formKey
        )
      )
  }

  @Test
  fun `should delete aggregate`() {

    val dataEntryChange = DataEntryChange(
      entryType = "io.holunda.My",
      entryId = UUID.randomUUID().toString(),
      applicationName = "myApp",
      name = "My Entry 4711",
      type = "My",
      payload = Variables.createVariables(),
      correlations = newCorrelations()
    )

    val command = DeleteDataEntryCommand(
      entryType = dataEntryChange.entryType,
      entryId = dataEntryChange.entryId,
      modification = Modification(OffsetDateTime.now(), "kermit", "kermit decided to delete", logNotes = "Let us delete this item"),
      state = ProcessingType.DELETED.of("deleted as irrelevant")
    )

    fixture
      .registerInjectableResource(LAX_POLICY)
      .given(
        DataEntryCreatedEvent(
          entryId = dataEntryChange.entryId,
          entryType = dataEntryChange.entryType,
          name = "Some name",
          type = "Another",
          applicationName = "Different application",
          state = dataEntryChange.state,
          description = dataEntryChange.description,
          payload = dataEntryChange.payload,
          correlations = dataEntryChange.correlations,
          createModification = dataEntryChange.modification,
          authorizations = dataEntryChange.authorizationChanges,
          formKey = dataEntryChange.formKey
        )
      )
      .`when`(command)
      .expectEvents(
        DataEntryDeletedEvent(
          entryId = command.entryId,
          entryType = command.entryType,
          deleteModification = command.modification,
          state = command.state
        )
      )
  }

  @Test
  fun `should not delete deleted aggregate in strict mode`() {

    val dataEntryChange = DataEntryChange(
      entryType = "io.holunda.My",
      entryId = UUID.randomUUID().toString(),
      applicationName = "myApp",
      name = "My Entry 4711",
      type = "My",
      payload = Variables.createVariables(),
      correlations = newCorrelations()
    )

    val command = DeleteDataEntryCommand(
      entryType = dataEntryChange.entryType,
      entryId = dataEntryChange.entryId,
      modification = Modification(OffsetDateTime.now(), "kermit", "kermit decided to delete", logNotes = "Let us delete this item"),
      state = ProcessingType.DELETED.of("deleted as irrelevant")
    )

    val deleteCommand = DeleteDataEntryCommand(
      entryType = dataEntryChange.entryType,
      entryId = dataEntryChange.entryId,
      modification = Modification(OffsetDateTime.now(), "kermit", "kermit decided to delete", logNotes = "Let us delete this item"),
      state = ProcessingType.DELETED.of("deleted as irrelevant")
    )


    fixture
      .registerInjectableResource(STRICT_POLICY)
      .given(
        DataEntryCreatedEvent(
          entryId = dataEntryChange.entryId,
          entryType = dataEntryChange.entryType,
          name = "Some name",
          type = "Another",
          applicationName = "Different application",
          state = dataEntryChange.state,
          description = dataEntryChange.description,
          payload = dataEntryChange.payload,
          correlations = dataEntryChange.correlations,
          createModification = dataEntryChange.modification,
          authorizations = dataEntryChange.authorizationChanges,
          formKey = dataEntryChange.formKey
        ),
        DataEntryDeletedEvent(
          entryId = command.entryId,
          entryType = command.entryType,
          deleteModification = command.modification,
          state = command.state
        )
      )
      .`when`(deleteCommand)
      .expectException(AggregateDeletedException::class.java)

  }

  @Test
  fun `should not update deleted aggregate on strict deletion policy`() {

    val dataEntryChange = DataEntryChange(
      entryType = "io.holunda.My",
      entryId = UUID.randomUUID().toString(),
      applicationName = "myApp",
      name = "My Entry 4711",
      type = "My",
      payload = Variables.createVariables(),
      correlations = newCorrelations()
    )

    val command = DeleteDataEntryCommand(
      entryType = dataEntryChange.entryType,
      entryId = dataEntryChange.entryId,
      modification = Modification(OffsetDateTime.now(), "kermit", "kermit decided to delete", logNotes = "Let us delete this item"),
      state = ProcessingType.DELETED.of("deleted as irrelevant")
    )

    val updateCommand = UpdateDataEntryCommand(
      dataEntryChange = DataEntryChange(
        entryType = dataEntryChange.entryType,
        entryId = dataEntryChange.entryId,
        applicationName = "myApp",
        name = "My Entry 4711",
        type = "My",
        payload = Variables.createVariables(),
        correlations = newCorrelations()
      )
    )


    fixture
      .registerInjectableResource(STRICT_POLICY)
      .given(
        DataEntryCreatedEvent(
          entryId = dataEntryChange.entryId,
          entryType = dataEntryChange.entryType,
          name = "Some name",
          type = "Another",
          applicationName = "Different application",
          state = dataEntryChange.state,
          description = dataEntryChange.description,
          payload = dataEntryChange.payload,
          correlations = dataEntryChange.correlations,
          createModification = dataEntryChange.modification,
          authorizations = dataEntryChange.authorizationChanges,
          formKey = dataEntryChange.formKey
        ),
        DataEntryDeletedEvent(
          entryId = command.entryId,
          entryType = command.entryType,
          deleteModification = command.modification,
          state = command.state
        )
      )
      .`when`(updateCommand)
      .expectException(AggregateDeletedException::class.java)

  }

  @Test
  fun `should recover deleted aggregate on update by lax deletion policy`() {

    val dataEntryChange = DataEntryChange(
      entryType = "io.holunda.My",
      entryId = UUID.randomUUID().toString(),
      applicationName = "myApp",
      name = "My Entry 4711",
      type = "My",
      payload = Variables.createVariables(),
      correlations = newCorrelations()
    )

    val command = DeleteDataEntryCommand(
      entryType = dataEntryChange.entryType,
      entryId = dataEntryChange.entryId,
      modification = Modification(OffsetDateTime.now(), "kermit", "kermit decided to delete", logNotes = "Let us delete this item"),
      state = ProcessingType.DELETED.of("deleted as irrelevant")
    )

    val updateCommand = UpdateDataEntryCommand(
      dataEntryChange = DataEntryChange(
        entryType = dataEntryChange.entryType,
        entryId = dataEntryChange.entryId,
        applicationName = "myApp",
        name = "My Entry 4711",
        type = "My",
        payload = Variables.createVariables(),
        correlations = newCorrelations()
      )
    )


    fixture
      .registerInjectableResource(LAX_POLICY)
      .given(
        DataEntryCreatedEvent(
          entryId = dataEntryChange.entryId,
          entryType = dataEntryChange.entryType,
          name = "Some name",
          type = "Another",
          applicationName = "Different application",
          state = dataEntryChange.state,
          description = dataEntryChange.description,
          payload = dataEntryChange.payload,
          correlations = dataEntryChange.correlations,
          createModification = dataEntryChange.modification,
          authorizations = dataEntryChange.authorizationChanges,
          formKey = dataEntryChange.formKey
        ),
        DataEntryDeletedEvent(
          entryId = command.entryId,
          entryType = command.entryType,
          deleteModification = command.modification,
          state = command.state
        )
      )
      .`when`(updateCommand)
      .expectEvents(
        DataEntryUpdatedEvent(
          entryId = updateCommand.dataEntryChange.entryId,
          entryType = updateCommand.dataEntryChange.entryType,
          name = updateCommand.dataEntryChange.name,
          type = updateCommand.dataEntryChange.type,
          applicationName = updateCommand.dataEntryChange.applicationName,
          state = updateCommand.dataEntryChange.state,
          description = updateCommand.dataEntryChange.description,
          payload = updateCommand.dataEntryChange.payload,
          correlations = updateCommand.dataEntryChange.correlations,
          updateModification = updateCommand.dataEntryChange.modification,
          authorizations = updateCommand.dataEntryChange.authorizationChanges,
          formKey = updateCommand.dataEntryChange.formKey
        )
      )
  }

  @Test
  fun `should serialize and deserialize an empty aggregate`() {
    // GIVEN an object mapper and an empty aggregate
    val objectMapper = ObjectMapper().configurePolyflowJacksonObjectMapper()
    val dataEntryAggregate = DataEntryAggregate()

    // WHEN we serialize and deserialize it
    val serializedObj = objectMapper.writeValueAsString(dataEntryAggregate)
    objectMapper.readValue(serializedObj, DataEntryAggregate::class.java)

    // THEN no problem should occur
  }

  @Test
  fun `should serialize and deserialize an aggregate with data`() {
    // GIVEN an object mapper and a filled aggregate
    val objectMapper = ObjectMapper()
      .configurePolyflowJacksonObjectMapper()
      .configurePolyflowJacksonObjectMapperForDatapool()

    val dataEntryAggregate = DataEntryAggregate().apply {
      on(
        DataEntryCreatedEvent(
          entryId = UUID.randomUUID().toString(),
          entryType = "io.holunda.My",
          name = "Some name",
          type = "Another",
          applicationName = "Different application",
        )
      )
    }
    val dataIdentityValue = dataEntryAggregate.getDataIdentityValueForTest()

    // WHEN we serialize and deserialize it
    val serializedObj = objectMapper.writeValueAsString(dataEntryAggregate)
    val deserializedObj = objectMapper.readValue(serializedObj, DataEntryAggregate::class.java)

    // THEN no problem should occur
    assertThat(deserializedObj.getDataIdentityValueForTest()).isEqualTo(dataIdentityValue)
  }

}

private fun DataEntryAggregate.getDataIdentityValueForTest(): String? {
  return javaClass.getDeclaredField("dataIdentity").let {
    it.isAccessible = true
    val value = it.get(this) as String?
    return@let value
  }
}
