package io.holunda.camunda.taskpool.api.sender

import io.holunda.camunda.taskpool.api.task.AssignTaskCommand
import io.holunda.camunda.taskpool.api.task.CompleteTaskCommand
import io.holunda.camunda.taskpool.api.task.CreateTaskCommand
import io.holunda.camunda.taskpool.api.task.DeleteTaskCommand

/**
 * Sender of task commands.
 */
interface TaskCommandSender {

  /**
   * Sends command for task assignment.
   */
  fun sendTaskCommand(command: AssignTaskCommand)

  /**
   * Sends command for task creation.
   */
  fun sendTaskCommand(command: CreateTaskCommand)

  /**
   * Sends command for task completion.
   */
  fun sendTaskCommand(command: CompleteTaskCommand)

  /**
   * Sends command for task deletion.
   */
  fun sendTaskCommand(command: DeleteTaskCommand)
}
