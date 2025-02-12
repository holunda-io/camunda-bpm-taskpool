package io.holunda.polyflow.datapool.sender.gateway

import org.axonframework.commandhandling.CommandResultMessage
import org.slf4j.Logger

/**
 * Error handler, logging the error.
 */
open class LoggingDataEntryCommandErrorHandler(private val logger: Logger) : CommandErrorHandler {

  override fun apply(commandMessage: Any, commandResultMessage: CommandResultMessage<out Any?>) {
    logger.error("SENDER-103: Sending command $commandMessage resulted in error", commandResultMessage.exceptionResult())
  }
}
