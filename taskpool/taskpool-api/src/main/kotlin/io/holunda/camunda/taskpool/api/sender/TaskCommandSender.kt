package io.holunda.camunda.taskpool.api.sender

import io.holunda.camunda.taskpool.api.task.*

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

  /**
   * Sends command for task update.
   */
  fun sendTaskCommand(command: UpdateAttributeTaskCommand)

  /**
   * Sends command for task update.
   */
  fun sendTaskCommand(command: UpdateAssignmentTaskCommand)

}
