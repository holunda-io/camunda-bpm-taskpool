package io.holunda.camunda.taskpool.sender

import io.holunda.camunda.taskpool.api.task.EngineTaskCommand
import io.holunda.camunda.taskpool.sender.gateway.CommandListGateway
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Sends commands using the gateway.
 */
class SimpleEngineTaskCommandSender(
  private val commandListGateway: CommandListGateway
) : EngineTaskCommandSender {

  private val logger: Logger = LoggerFactory.getLogger(EngineTaskCommandSender::class.java)

  override fun send(command: EngineTaskCommand) {
    commandListGateway.sendToGateway(listOf(command))
  }

}
