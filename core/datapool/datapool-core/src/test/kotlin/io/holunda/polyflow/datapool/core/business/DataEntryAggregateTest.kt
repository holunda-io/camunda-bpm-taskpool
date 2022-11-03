package io.holunda.polyflow.datapool.core.business

import io.holunda.camunda.taskpool.api.business.*
import org.axonframework.eventsourcing.AggregateDeletedException
import org.axonframework.test.aggregate.AggregateTestFixture
import org.camunda.bpm.engine.variable.Variables
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import java.util.*

class DataEntryAggregateTest {

  private val fixture = AggregateTestFixture(DataEntryAggregate::class.java)

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
      ).expectMarkedDeleted()

  }

  @Test
  fun `should not delete deleted aggregate`() {

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
  fun `should not update deleted aggregate`() {

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


}
