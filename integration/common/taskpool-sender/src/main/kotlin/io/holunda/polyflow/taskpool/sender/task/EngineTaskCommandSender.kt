package io.holunda.polyflow.taskpool.sender.task

import io.holunda.camunda.taskpool.api.task.EngineTaskCommand

/**
 * Interface for beans sending task commands.
 */
interface EngineTaskCommandSender {
  /**
   * Sends the command to core and enriches it, if possible.
   */
  fun send(command: EngineTaskCommand)
}
