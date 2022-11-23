package io.holunda.camunda.taskpool.api.business

import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.variable.Variables.createVariables
import org.junit.jupiter.api.Test


internal class MappersTest {


  @Test
  fun `maps create command to event `() {
    val command = CreateDataEntryCommand(
      dataEntryChange = DataEntryChange(
        entryType = "TestType",
        entryId = "4711",
        type = "Test Type",
        applicationName = "business-app",
        name = "Test Item",
        correlations = newCorrelations().putValue("PurchaseOrder", "4712"),
        payload = createVariables().putValue("Key", "Value"),
        description = "Test Type described carefully",
        state = ProcessingType.IN_PROGRESS.of("doing"),
        modification = Modification.now().copy(username = "user", log = "log", logNotes = "logNotes"),
        authorizationChanges = listOf(AuthorizationChange.addUser("user")),
        formKey = "form"
      )
    )

    val event = command.createdEvent()
    assertThat(event.entryId).isEqualTo(command.dataEntryChange.entryId)
    assertThat(event.entryType).isEqualTo(command.dataEntryChange.entryType)
    assertThat(event.type).isEqualTo(command.dataEntryChange.type)
    assertThat(event.applicationName).isEqualTo(command.dataEntryChange.applicationName)
    assertThat(event.name).isEqualTo(command.dataEntryChange.name)
    assertThat(event.correlations).isEqualTo(command.dataEntryChange.correlations)
    assertThat(event.payload).isEqualTo(command.dataEntryChange.payload)
    assertThat(event.state).isEqualTo(command.dataEntryChange.state)
    assertThat(event.createModification).isEqualTo(command.dataEntryChange.modification)
    assertThat(event.authorizations).isEqualTo(command.dataEntryChange.authorizationChanges)
    assertThat(event.formKey).isEqualTo(command.dataEntryChange.formKey)
  }

  @Test
  fun `maps update command to event `() {
    val command = UpdateDataEntryCommand(
      dataEntryChange = DataEntryChange(
        entryType = "TestType",
        entryId = "4711",
        type = "Test Type",
        applicationName = "business-app",
        name = "Test Item",
        correlations = newCorrelations().putValue("PurchaseOrder", "4712"),
        payload = createVariables().putValue("Key", "Value"),
        description = "Test Type described carefully",
        state = ProcessingType.IN_PROGRESS.of("doing"),
        modification = Modification.now().copy(username = "user", log = "log", logNotes = "logNotes"),
        authorizationChanges = listOf(AuthorizationChange.addUser("user")),
        formKey = "form"
      )
    )

    val event = command.updatedEvent()
    assertThat(event.entryId).isEqualTo(command.dataEntryChange.entryId)
    assertThat(event.entryType).isEqualTo(command.dataEntryChange.entryType)
    assertThat(event.type).isEqualTo(command.dataEntryChange.type)
    assertThat(event.applicationName).isEqualTo(command.dataEntryChange.applicationName)
    assertThat(event.name).isEqualTo(command.dataEntryChange.name)
    assertThat(event.correlations).isEqualTo(command.dataEntryChange.correlations)
    assertThat(event.payload).isEqualTo(command.dataEntryChange.payload)
    assertThat(event.state).isEqualTo(command.dataEntryChange.state)
    assertThat(event.updateModification).isEqualTo(command.dataEntryChange.modification)
    assertThat(event.authorizations).isEqualTo(command.dataEntryChange.authorizationChanges)
    assertThat(event.formKey).isEqualTo(command.dataEntryChange.formKey)
  }

  @Test
  fun `maps delete command to event `() {
    val command = DeleteDataEntryCommand(
      entryType = "TestType",
      entryId = "4711",
      modification = Modification.now().copy(username = "user", log = "log", logNotes = "logNotes"),
      state = ProcessingType.IN_PROGRESS.of("doing")
    )
    val event = command.deletedEvent()
    assertThat(event.entryId).isEqualTo(command.entryId)
    assertThat(event.entryType).isEqualTo(command.entryType)
    assertThat(event.state).isEqualTo(command.state)
    assertThat(event.deleteModification).isEqualTo(command.modification)

  }

}