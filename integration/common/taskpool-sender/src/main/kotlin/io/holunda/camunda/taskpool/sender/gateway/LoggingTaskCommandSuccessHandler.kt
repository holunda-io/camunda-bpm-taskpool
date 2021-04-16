package io.holunda.camunda.taskpool.sender.gateway

import org.axonframework.commandhandling.CommandResultMessage
import org.slf4j.Logger

/**
 * Logs success.
 */
open class LoggingTaskCommandSuccessHandler(private val logger: Logger) : CommandSuccessHandler {

  override fun apply(commandMessage: Any, commandResultMessage: CommandResultMessage<out Any?>) {
    if (logger.isDebugEnabled) {
      logger.debug("SENDER-002: Successfully submitted command $commandMessage")
    }
  }
}
