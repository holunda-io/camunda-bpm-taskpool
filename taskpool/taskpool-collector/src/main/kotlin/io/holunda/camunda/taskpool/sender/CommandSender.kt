package io.holunda.camunda.taskpool.sender

import io.holunda.camunda.taskpool.api.task.WithTaskId

/**
 * Interface for beans sending commands.
 */
interface CommandSender {
  /**
   * Sends the command to core and enriches it, if possible.
   */
  fun send(command: WithTaskId)
}
