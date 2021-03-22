package io.holunda.camunda.taskpool.sender.process.instance

import io.holunda.camunda.taskpool.api.process.definition.ProcessDefinitionCommand

/**
 * Sender for process instances.
 */
interface ProcessInstanceCommandSender {

  /**
   * Sends a process instance command.
   * @param command command to send.
   */
  fun send(command: ProcessDefinitionCommand)
}
