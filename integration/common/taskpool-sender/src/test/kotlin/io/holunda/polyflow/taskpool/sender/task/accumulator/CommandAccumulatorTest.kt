package io.holunda.polyflow.taskpool.sender.task.accumulator

import io.holunda.camunda.taskpool.api.task.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CommandAccumulatorTest {

  private val createTaskCommand = CreateTaskCommand(
    id = "d7c7efe2-0475-11e9-90f1-a0c589a3e9e5",
    sourceReference = ProcessReference(
      instanceId = "d7c7c8cd-0475-11e9-90f1-a0c589a3e9e5",
      executionId = "d7c7c8cd-0475-11e9-90f1-a0c589a3e9e5",
      definitionId = "processId:4:d7c6de6c-0475-11e9-90f1-a0c589a3e9e5",
      name = "My Process",
      definitionKey = "processId",
      applicationName = "command-projector-test"
    ),
    taskDefinitionKey = "userTask-123"
  )

  private val updateAttributeTaskCommand = UpdateAttributeTaskCommand(
    id = "d7c7efe2-0475-11e9-90f1-a0c589a3e9e5",
    priority = 50,
    owner = null,
    description = "some description",
    name = "task name",
    taskDefinitionKey = "userTask-123",
    sourceReference = ProcessReference(
      instanceId = "d7c7c8cd-0475-11e9-90f1-a0c589a3e9e5",
      executionId = "d7c7c8cd-0475-11e9-90f1-a0c589a3e9e5",
      definitionId = "processId:4:d7c6de6c-0475-11e9-90f1-a0c589a3e9e5",
      name = "My Process",
      definitionKey = "processId",
      applicationName = "command-projector-test"
    )
  )

  private val completeTaskCommand = CompleteTaskCommand(
    id = "d7c7efe2-0475-11e9-90f1-a0c589a3e9e5"
  )

  private val deleteTaskCommand = DeleteTaskCommand(
    id = "d7c7efe2-0475-11e9-90f1-a0c589a3e9e5",
    deleteReason = "wtf?"
  )

  @Test
  fun `should sort accumulate commands`() {
    val sortingCommandAccumulator = SortingCommandAccumulator()
    val collectedTaskCommands = listOf(deleteTaskCommand, updateAttributeTaskCommand, createTaskCommand, completeTaskCommand)
    val sortedTaskCommands = sortingCommandAccumulator.invoke(collectedTaskCommands)
    assertThat(sortedTaskCommands).containsExactly(createTaskCommand, completeTaskCommand, deleteTaskCommand, updateAttributeTaskCommand)
  }


}
