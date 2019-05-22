package io.holunda.camunda.datapool.core.business

import io.holunda.camunda.taskpool.api.business.*
import org.axonframework.test.aggregate.AggregateTestFixture
import org.camunda.bpm.engine.variable.Variables
import org.junit.Test
import java.util.*

class DataEntryAggregateTest {

  val fixture = AggregateTestFixture<DataEntryAggregate>(DataEntryAggregate::class.java)

  @Test
  fun `should create aggregate`() {

    val command = CreateDataEntryCommand(
      dataEntry = DataEntry(
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
        entryId = command.dataEntry.entryId,
        entryType = command.dataEntry.entryType,
        name = command.dataEntry.name,
        type = command.dataEntry.type,
        applicationName = command.dataEntry.applicationName,
        state = command.dataEntry.state,
        description = command.dataEntry.description,
        payload = command.dataEntry.payload,
        correlations = command.dataEntry.correlations,
        createModification = command.dataEntry.modification,
        authorizedUsers = command.dataEntry.authorizedUsers,
        authorizedGroups = command.dataEntry.authorizedGroups,
        formKey = command.dataEntry.formKey
      ))
  }

  @Test
  fun `should update aggregate`() {

    val command = UpdateDataEntryCommand(
      dataEntry = DataEntry(
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
          entryId = command.dataEntry.entryId,
          entryType = command.dataEntry.entryType,
          name = "Some name",
          type = "Another",
          applicationName = "Different application",
          state = command.dataEntry.state,
          description = command.dataEntry.description,
          payload = command.dataEntry.payload,
          correlations = command.dataEntry.correlations,
          createModification = command.dataEntry.modification,
          authorizedUsers = command.dataEntry.authorizedUsers,
          authorizedGroups = command.dataEntry.authorizedGroups,
          formKey = command.dataEntry.formKey
          )
        )
      .`when`(command)
      .expectEvents(DataEntryUpdatedEvent(
        entryId = command.dataEntry.entryId,
        entryType = command.dataEntry.entryType,
        name = command.dataEntry.name,
        type = command.dataEntry.type,
        applicationName = command.dataEntry.applicationName,
        state = command.dataEntry.state,
        description = command.dataEntry.description,
        payload = command.dataEntry.payload,
        correlations = command.dataEntry.correlations,
        updateModification = command.dataEntry.modification,
        authorizedUsers = command.dataEntry.authorizedUsers,
        authorizedGroups = command.dataEntry.authorizedGroups,
        formKey = command.dataEntry.formKey
      ))

  }
}
