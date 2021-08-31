package io.holunda.polyflow.taskpool.sender.process.instance

import io.holunda.camunda.taskpool.api.process.instance.ProcessInstanceCommand
import io.holunda.polyflow.taskpool.sender.SenderProperties
import io.holunda.polyflow.taskpool.sender.gateway.CommandListGateway
import mu.KLogging

/**
 * Simple sender for process definition commands
 */
internal class SimpleProcessInstanceCommandSender(
  private val commandListGateway: CommandListGateway,
  private val senderProperties: SenderProperties
) : ProcessInstanceCommandSender {

  companion object : KLogging()

  override fun send(command: ProcessInstanceCommand) {
    if (senderProperties.enabled && senderProperties.processInstance.enabled) {
      commandListGateway.sendToGateway(listOf(command))
    } else {
      logger.debug { "SENDER-008: Process instance sending is disabled by property. Would have sent $command." }
    }
  }
}
