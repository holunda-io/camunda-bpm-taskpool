package io.holunda.polyflow.taskpool.sender

import io.holunda.camunda.taskpool.api.task.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.*

class CommandSorterTest {

  val createTaskCommand = CreateTaskCommand(
    id = "some-id",
    sourceReference = makeProcessReference(),
    taskDefinitionKey = "task-definition-key-abcde"


  )

  val assignTaskCommand = AssignTaskCommand(
    id = "some-id",
    assignee = "kermit"
  )

  val updateTaskCommand = UpdateAttributeTaskCommand(
    id = "some-id",
    name = "task name",
    description = "task description",
    owner = null,
    dueDate = Date(1234567890L),
    priority = 0,
    sourceReference = makeProcessReference(),
    taskDefinitionKey = "task-definition-key-abcde"
  )

  val completeTaskCommand = CompleteTaskCommand(
    id = "some-id"
  )

  val deleteTaskCommand = DeleteTaskCommand(
    id = "some-id",
    deleteReason = "some delete reason"
  )

  val addCandidateUserCommand = AddCandidateUsersCommand(
    id = "some-id",
    candidateUsers = setOf("piggy")
  )

  val deleteCandidateUserCommand = DeleteCandidateUsersCommand(
    id = "some-id",
    candidateUsers = setOf("piggy")
  )

  val addCandidateGroupCommand = AddCandidateGroupsCommand(
    id = "some-id",
    candidateGroups = setOf("muppetshow")
  )

  val deleteCandidateGroupCommand = DeleteCandidateGroupsCommand(
    id = "some-id",
    candidateGroups = setOf("muppetshow")
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
  fun `create task commands should come after all other events`() {
    listOf(assignTaskCommand, updateTaskCommand, completeTaskCommand, deleteTaskCommand,
      addCandidateUserCommand, deleteCandidateUserCommand, addCandidateGroupCommand, deleteCandidateGroupCommand).forEach {
      assertThat(EngineTaskCommandSorter().compare(createTaskCommand, it)).isEqualTo(-1)
      assertThat(EngineTaskCommandSorter().compare(it, createTaskCommand)).isEqualTo(1)
    }
  }

  @Test
  fun `complete task commands should come after all other events except for create`() {
    listOf(assignTaskCommand, updateTaskCommand, deleteTaskCommand,
      addCandidateUserCommand, deleteCandidateUserCommand, addCandidateGroupCommand, deleteCandidateGroupCommand).forEach {
      assertThat(EngineTaskCommandSorter().compare(completeTaskCommand, it)).isEqualTo(-1)
      assertThat(EngineTaskCommandSorter().compare(it, completeTaskCommand)).isEqualTo(1)
    }
    assertThat(EngineTaskCommandSorter().compare(completeTaskCommand, createTaskCommand)).isEqualTo(1)
    assertThat(EngineTaskCommandSorter().compare(createTaskCommand, completeTaskCommand)).isEqualTo(-1)
  }

  @Test
  fun `delete task commands should come after all other events except create and complete task`() {
    listOf(assignTaskCommand, updateTaskCommand,
      addCandidateUserCommand, deleteCandidateUserCommand, addCandidateGroupCommand, deleteCandidateGroupCommand).forEach {
      assertThat(EngineTaskCommandSorter().compare(deleteTaskCommand, it)).isEqualTo(-1)
      assertThat(EngineTaskCommandSorter().compare(it, deleteTaskCommand)).isEqualTo(1)
    }
    assertThat(EngineTaskCommandSorter().compare(createTaskCommand, deleteTaskCommand)).isEqualTo(-1)
    assertThat(EngineTaskCommandSorter().compare(deleteTaskCommand, createTaskCommand)).isEqualTo(1)

    assertThat(EngineTaskCommandSorter().compare(completeTaskCommand, deleteTaskCommand)).isEqualTo(-1)
    assertThat(EngineTaskCommandSorter().compare(deleteTaskCommand, completeTaskCommand)).isEqualTo(1)
  }

  @Test
  fun `sorting must be consistent`() {
    listOf(createTaskCommand, assignTaskCommand, updateTaskCommand, completeTaskCommand, deleteTaskCommand,
      addCandidateUserCommand, deleteCandidateUserCommand, addCandidateGroupCommand, deleteCandidateGroupCommand).forEach {
      assertThat(EngineTaskCommandSorter().compare(it, it)).isEqualTo(0)
    }
  }

}
