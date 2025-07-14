package io.holunda.polyflow.datapool.sender.gateway

import io.github.oshai.kotlinlogging.KLogger
import org.axonframework.commandhandling.CommandResultMessage

/**
 * Logs success.
 */
open class LoggingDataEntryCommandSuccessHandler(private val logger: KLogger) : CommandSuccessHandler {

  override fun apply(commandMessage: Any, commandResultMessage: CommandResultMessage<out Any?>) {
    if (logger.isDebugEnabled()) {
      logger.debug { "SENDER-102: Successfully submitted command $commandMessage" }
    }
  }
}
