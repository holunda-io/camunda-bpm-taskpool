package io.holunda.polyflow.taskpool.sender.process.instance

import io.holunda.camunda.taskpool.api.process.instance.ProcessInstanceCommand

/**
 * Sender for process instances.
 */
interface ProcessInstanceCommandSender {

  /**
   * Sends a process instance command.
   * @param command command to send.
   */
  fun send(command: ProcessInstanceCommand)
}
