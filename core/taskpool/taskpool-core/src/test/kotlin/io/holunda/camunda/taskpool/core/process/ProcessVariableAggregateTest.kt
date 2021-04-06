package io.holunda.camunda.taskpool.core.process

import io.holunda.camunda.taskpool.api.process.variable.*
import io.holunda.camunda.taskpool.api.task.ProcessReference
import org.axonframework.test.aggregate.AggregateTestFixture
import org.junit.Test

class ProcessVariableAggregateTest {

  private val fixture = AggregateTestFixture(ProcessVariableAggregate::class.java)
  private val sourceReference = ProcessReference(
    "id:1",
    "id:1",
    "process-definition-key:67",
    "process-definition-key",
    "My Process",
    "My application",
    null
  )


  @Test
  fun `should create aggregate on variable create`() {

    val command = CreateProcessVariableCommand(
      variableInstanceId = "1",
      variableName = "name",
      sourceReference = sourceReference,
      scopeActivityInstanceId = "scope001",
      value = PrimitiveProcessVariableValue("kermit"),
      revision = 1
    )

    fixture
      .givenNoPriorActivity()
      .`when`(command)
      .expectEvents(ProcessVariableCreatedEvent(
        command.sourceReference,
        command.variableName,
        command.variableInstanceId,
        command.scopeActivityInstanceId,
        command.value
      ))
  }


  @Test
  fun `should delete aggregate on variable delete`() {

    val command = DeleteProcessVariableCommand(
      variableInstanceId = "1",
      variableName = "name",
      sourceReference = sourceReference,
      scopeActivityInstanceId = "scope001",
    )

    fixture
      .given(ProcessVariableCreatedEvent(
        command.sourceReference,
        command.variableName,
        command.variableInstanceId,
        command.scopeActivityInstanceId,
        PrimitiveProcessVariableValue("1")
      ))
      .`when`(command)
      .expectEvents(ProcessVariableDeletedEvent(
        command.sourceReference,
        command.variableName,
        command.variableInstanceId,
        command.scopeActivityInstanceId
      ))
  }

}
