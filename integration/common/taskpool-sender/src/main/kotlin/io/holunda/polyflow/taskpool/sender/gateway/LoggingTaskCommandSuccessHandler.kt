package io.holunda.polyflow.taskpool.sender.gateway

import io.github.oshai.kotlinlogging.KLogger
import org.axonframework.commandhandling.CommandResultMessage

/**
 * Logs success.
 */
open class LoggingTaskCommandSuccessHandler(private val logger: KLogger) : CommandSuccessHandler {

  override fun apply(commandMessage: Any, commandResultMessage: CommandResultMessage<out Any?>) {
    if (logger.isDebugEnabled()) {
      logger.debug { "SENDER-002: Successfully submitted command $commandMessage" }
    }
  }
}
