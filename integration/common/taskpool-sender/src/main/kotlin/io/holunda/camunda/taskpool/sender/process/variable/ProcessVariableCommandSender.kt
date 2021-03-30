package io.holunda.camunda.taskpool.sender.process.variable

import io.holunda.camunda.taskpool.api.process.definition.ProcessDefinitionCommand
import io.holunda.camunda.taskpool.api.process.variable.ProcessVariableCommand

interface ProcessVariableCommandSender {
  /**
   * Sends a process variable command.
   * @param command command to send.
   */
  fun send(command: ProcessVariableCommand)

}
