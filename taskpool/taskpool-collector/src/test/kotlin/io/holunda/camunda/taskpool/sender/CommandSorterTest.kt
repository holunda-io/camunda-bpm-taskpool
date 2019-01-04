package io.holunda.camunda.taskpool.sender

import io.holunda.camunda.taskpool.api.task.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.util.*

class CommandSorterTest {

  val createTaskCommand = CreateTaskCommand(
    id = "some-id",
    sourceReference = makeProcessReference(),
    taskDefinitionKey = "task-definition-key-abcde"
  )

  val assignTaskCommand = AssignTaskCommand(
    id = "some-id",
    sourceReference = makeProcessReference(),
    taskDefinitionKey = "task-definition-key-abcde",
    assignee = "kermit"
  )

  val updateTaskCommand = UpdateAttributeTaskCommand(
    id = "some-id",
    sourceReference = makeProcessReference(),
    taskDefinitionKey = "task-definition-key-abcde",
    name = "task name",
    description = "task description",
    owner = null,
    assignee = "kermit",
    dueDate = Date(1234567890L),
    priority = 0
  )

  val completeTaskCommand = CompleteTaskCommand(
    id = "some-id",
    sourceReference = makeProcessReference(),
    taskDefinitionKey = "task-definition-key-abcde"
  )

  val deleteTaskCommand = DeleteTaskCommand(
    id = "some-id",
    sourceReference = makeProcessReference(),
    taskDefinitionKey = "task-definition-key-abcde",
    deleteReason = "some delete reason"
  )

  val addCandidateUserCommand = AddCandidateUserCommand(
    id = "some-id",
    userId = "piggy"
  )

  val deleteCandidateUserCommand = DeleteCandidateUserCommand(
    id = "some-id",
    userId = "piggy"
  )

  val addCandidateGroupCommand = AddCandidateGroupCommand(
    id = "some-id",
    groupId = "muppetshow"
  )

  val deleteCandidateGroupCommand = DeleteCandidateGroupCommand(
    id = "some-id",
    groupId = "muppetshow"
  )

  private fun makeProcessReference() = ProcessReference(
    "instance-id-12345",
    "execution-id-12345",
    "definition-id-12345",
    "definition-key-abcde",
    "process-name",
    "application-name"
  )

  @Test
  fun `create task commands should precede other events`() {
    listOf<WithTaskId>(assignTaskCommand, updateTaskCommand, completeTaskCommand, deleteTaskCommand,
        addCandidateUserCommand, deleteCandidateUserCommand, addCandidateGroupCommand, deleteCandidateGroupCommand).forEach {
      assertThat(CommandSorter().compare(createTaskCommand, it)).isEqualTo(-1)
      assertThat(CommandSorter().compare(it, createTaskCommand)).isEqualTo(1)
    }
  }

  @Test
  fun `delete task commands should come after all other events`() {
    listOf<WithTaskId>(createTaskCommand, assignTaskCommand, updateTaskCommand, completeTaskCommand,
        addCandidateUserCommand, deleteCandidateUserCommand, addCandidateGroupCommand, deleteCandidateGroupCommand).forEach {
      assertThat(CommandSorter().compare(deleteTaskCommand, it)).isEqualTo(1)
      assertThat(CommandSorter().compare(it, deleteTaskCommand)).isEqualTo(-1)
    }
  }

  @Test
  fun `complete task commands should come after all other events except for delete task`() {
    listOf<WithTaskId>(createTaskCommand, assignTaskCommand, updateTaskCommand,
        addCandidateUserCommand, deleteCandidateUserCommand, addCandidateGroupCommand, deleteCandidateGroupCommand).forEach {
      assertThat(CommandSorter().compare(completeTaskCommand, it)).isEqualTo(1)
      assertThat(CommandSorter().compare(it, completeTaskCommand)).isEqualTo(-1)
    }
    assertThat(CommandSorter().compare(completeTaskCommand, deleteTaskCommand)).isEqualTo(-1)
    assertThat(CommandSorter().compare(deleteTaskCommand, completeTaskCommand)).isEqualTo(1)
  }

  @Test
  fun `sorting must be consistent`() {
    listOf<WithTaskId>(createTaskCommand, assignTaskCommand, updateTaskCommand, completeTaskCommand, deleteTaskCommand,
        addCandidateUserCommand, deleteCandidateUserCommand, addCandidateGroupCommand, deleteCandidateGroupCommand).forEach {
      assertThat(CommandSorter().compare(it, it)).isEqualTo(0)
    }
  }

}
