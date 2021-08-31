package io.holunda.polyflow.datapool.sender

import org.axonframework.commandhandling.CommandResultMessage
import org.slf4j.Logger

/**
 * Error handler, logging the error.
 */
class LoggingCommandErrorHandler(private val logger: Logger) : DataEntryCommandErrorHandler {

  override fun apply(commandMessage: Any, commandResultMessage: CommandResultMessage<out Any?>) {
    logger.error("SENDER-006: Sending command $commandMessage resulted in error", commandResultMessage.exceptionResult())
  }
}
