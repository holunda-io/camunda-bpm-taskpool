package io.holunda.polyflow.taskpool.sender.task

import io.holunda.camunda.taskpool.api.task.EngineTaskCommand
import io.holunda.polyflow.taskpool.sender.SenderProperties
import io.holunda.polyflow.taskpool.sender.gateway.CommandListGateway
import mu.KLogging

/**
 * Sends commands using the gateway.
 */
class SimpleEngineTaskCommandSender(
  private val commandListGateway: CommandListGateway,
  private val senderProperties: SenderProperties
) : EngineTaskCommandSender {

  companion object : KLogging()

  override fun send(command: EngineTaskCommand) {
    if (senderProperties.task.enabled) {
      commandListGateway.sendToGateway(listOf(command))
    } else {
      logger.debug { "SENDER-004: Process task sending is disabled by property. Would have sent $command." }
    }
  }

}
