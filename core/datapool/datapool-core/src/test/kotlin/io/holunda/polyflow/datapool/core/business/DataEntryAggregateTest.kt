package io.holunda.polyflow.datapool.core.business

import io.holunda.camunda.taskpool.api.business.*
import org.axonframework.test.aggregate.AggregateTestFixture
import org.camunda.bpm.engine.variable.Variables
import org.junit.jupiter.api.Test
import java.util.*

class DataEntryAggregateTest {

  val fixture = AggregateTestFixture(DataEntryAggregate::class.java)

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
      .expectEvents(DataEntryCreatedEvent(
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
      ))
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
      .expectEvents(DataEntryUpdatedEvent(
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
      ))

  }
}
