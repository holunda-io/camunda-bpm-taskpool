package io.holunda.camunda.taskpool.sender.gateway

import io.holunda.camunda.taskpool.SenderProperties
import org.axonframework.commandhandling.CommandResultMessage
import org.axonframework.commandhandling.gateway.CommandGateway
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Sends  a list commands via AXON command gateway one-by-one, only if the sender property is enabled.
 */
@Component
internal class AxonCommandListGateway(
  private val commandGateway: CommandGateway,
  private val properties: SenderProperties,
  private val commandSuccessHandler: CommandSuccessHandler,
  private val commandErrorHandler: CommandErrorHandler
) : CommandListGateway {

  private val logger: Logger = LoggerFactory.getLogger(CommandListGateway::class.java)

  /**
   * Sends data to gateway. Ignores any errors, but logs.
   */
  override fun sendToGateway(commands: List<Any>) {
    if (commands.isNotEmpty()) {

      val nextCommand = commands.first()
      val remainingCommands = commands.subList(1, commands.size)

      if (properties.enabled) {
        commandGateway.send<Any, Any?>(nextCommand) { commandMessage, commandResultMessage ->
          if (commandResultMessage.isExceptional) {
            commandErrorHandler.apply(commandMessage, commandResultMessage)
          } else {
            commandSuccessHandler.apply(commandMessage, commandResultMessage)
          }
          sendToGateway(remainingCommands)
        }
      } else {
        logger.debug("SENDER-003: Sending command over gateway disabled by property. Would have sent command $nextCommand")
        sendToGateway(remainingCommands)
      }
    }
  }

}

/**
 * Error handler, logging the error.
 */
open class LoggingTaskCommandErrorHandler(private val logger: Logger) : CommandErrorHandler {

  override fun apply(commandMessage: Any, commandResultMessage: CommandResultMessage<out Any?>) {
    logger.error("SENDER-006: Sending command $commandMessage resulted in error", commandResultMessage.exceptionResult())
  }
}

/**
 * Logs success.
 */
open class LoggingTaskCommandSuccessHandler(private val logger: Logger) : CommandSuccessHandler {

  override fun apply(commandMessage: Any, commandResultMessage: CommandResultMessage<out Any?>) {
    if (logger.isDebugEnabled) {
      logger.debug("SENDER-004: Successfully submitted command $commandMessage")
    }
  }
}



