package io.holunda.polyflow.taskpool.sender.process.instance

import io.github.oshai.kotlinlogging.KotlinLogging
import io.holunda.camunda.taskpool.api.process.instance.ProcessInstanceCommand
import io.holunda.polyflow.taskpool.sender.SenderProperties
import io.holunda.polyflow.taskpool.sender.gateway.CommandListGateway

private val logger = KotlinLogging.logger {}

/**
 * Simple sender for process definition commands
 */
internal class SimpleProcessInstanceCommandSender(
  private val commandListGateway: CommandListGateway,
  private val senderProperties: SenderProperties
) : ProcessInstanceCommandSender {

  override fun send(command: ProcessInstanceCommand) {
    if (senderProperties.enabled && senderProperties.processInstance.enabled) {
      commandListGateway.sendToGateway(listOf(command))
    } else {
      logger.debug { "SENDER-008: Process instance sending is disabled by property. Would have sent $command." }
    }
  }
}
