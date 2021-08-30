package io.holunda.polyflow.taskpool.sender.process.variable

/**
 * Sender for process variables.
 */
interface ProcessVariableCommandSender {
  /**
   * Sends a process variable command.
   * @param command command to send.
   */
  fun send(command: SingleProcessVariableCommand)
}
