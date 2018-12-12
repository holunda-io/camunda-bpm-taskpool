package io.holunda.camunda.taskpool.sender

/**
 * Interface for beans sending commands.
 */
interface CommandSender {
  /**
   * Sends the command to core and enriches it, if possible.
   */
  fun send(command: Any)
}
