package io.holunda.camunda.taskpool.sender.process.variable

import io.holunda.camunda.taskpool.api.process.variable.ProcessVariableCommand
import io.holunda.camunda.taskpool.sender.SenderProperties
import io.holunda.camunda.taskpool.sender.gateway.CommandListGateway
import mu.KLogging

/**
 * Simple sender for process variable commands.
 */
internal class SimpleProcessVariableCommandSender(
  private val commandListGateway: CommandListGateway,
  private val senderProperties: SenderProperties
): ProcessVariableCommandSender {
  companion object : KLogging()

  override fun send(command: ProcessVariableCommand) {
    if (senderProperties.processVariable.enabled) {
      commandListGateway.sendToGateway(listOf(command))
    } else {
      logger.debug { "SENDER-009: Process variable sending is disabled by property. Would have sent $command." }
    }
  }

}
