package io.holunda.polyflow.taskpool.sender.process.definition

import io.github.oshai.kotlinlogging.KotlinLogging
import io.holunda.camunda.taskpool.api.process.definition.ProcessDefinitionCommand
import io.holunda.polyflow.taskpool.sender.SenderProperties
import io.holunda.polyflow.taskpool.sender.gateway.CommandListGateway

private val logger = KotlinLogging.logger {}

/**
 * Simple sender for process definition commands
 */
internal class SimpleProcessDefinitionCommandSender(
  private val commandListGateway: CommandListGateway,
  private val senderProperties: SenderProperties
) : ProcessDefinitionCommandSender {

  override fun send(command: ProcessDefinitionCommand) {
    if (senderProperties.enabled && senderProperties.processDefinition.enabled) {
      commandListGateway.sendToGateway(listOf(command))
    } else {
      logger.debug { "SENDER-007: Process definition sending is disabled by property. Would have sent $command." }
    }
  }
}
