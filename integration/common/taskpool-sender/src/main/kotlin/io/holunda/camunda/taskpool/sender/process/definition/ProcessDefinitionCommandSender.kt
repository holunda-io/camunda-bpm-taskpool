package io.holunda.camunda.taskpool.sender.process.definition

import io.holunda.camunda.taskpool.api.process.definition.ProcessDefinitionCommand

/**
 * Sender for process deifnitions.
 */
interface ProcessDefinitionCommandSender {

  /**
   * Sends a process definition command.
   * @param command command to send.
   */
  fun send(command: ProcessDefinitionCommand)
}
