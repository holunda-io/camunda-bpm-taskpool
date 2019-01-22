package io.holunda.camunda.taskpool.sender.gateway

import io.holunda.camunda.taskpool.TaskCollectorProperties
import io.holunda.camunda.taskpool.sender.CommandSender
import org.axonframework.commandhandling.gateway.CommandGateway
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Sends  a list commands via AXON command gateway one-by-one, only if the sender property is enabled.
 */
@Component
open class AxonCommandListGateway(
  private val commandGateway: CommandGateway,
  private val properties: TaskCollectorProperties
) : CommandListGateway {

  private val logger: Logger = LoggerFactory.getLogger(CommandSender::class.java)

  /**
   * Sends data to gateway. Ignores any errors, but logs.
   */
  override fun sendToGateway(commands: List<Any>) {
    if (!commands.isEmpty()) {

      val nextCommand = commands.first()
      val remainingCommands = commands.subList(1, commands.size)

      if (properties.sender.enabled) {
        commandGateway.send<Any, Any?>(nextCommand) { commandMessage, commandResultMessage ->
          if (commandResultMessage.isExceptional) {
            logger.error("SENDER-006: Sending command $commandMessage resulted in error ${commandResultMessage.exceptionResult()}")
          } else {
            logger.debug("SENDER-004: Successfully submitted command $commandMessage")
          }
          sendToGateway(remainingCommands)
        }
      } else {
        logger.debug("SENDER-003: Would have sent command $nextCommand")
        sendToGateway(remainingCommands)
      }
    }
  }

}
