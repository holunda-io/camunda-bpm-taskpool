package io.holunda.polyflow.datapool.sender.gateway

import org.axonframework.commandhandling.CommandResultMessage
import org.slf4j.Logger

/**
 * Logs success.
 */
open class LoggingDataEntryCommandSuccessHandler(private val logger: Logger) : CommandSuccessHandler {

  override fun apply(commandMessage: Any, commandResultMessage: CommandResultMessage<out Any?>) {
    if (logger.isDebugEnabled) {
      logger.debug("SENDER-102: Successfully submitted command $commandMessage")
    }
  }
}
