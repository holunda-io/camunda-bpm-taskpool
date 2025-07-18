package io.holunda.polyflow.datapool.sender.gateway

import io.github.oshai.kotlinlogging.KLogger
import org.axonframework.commandhandling.CommandResultMessage

/**
 * Error handler, logging the error.
 */
open class LoggingDataEntryCommandErrorHandler(private val logger: KLogger) : CommandErrorHandler {

  override fun apply(commandMessage: Any, commandResultMessage: CommandResultMessage<out Any?>) {
    logger.error(commandResultMessage.exceptionResult()) { "${"SENDER-103: Sending command $commandMessage resulted in error"}" }
  }
}
