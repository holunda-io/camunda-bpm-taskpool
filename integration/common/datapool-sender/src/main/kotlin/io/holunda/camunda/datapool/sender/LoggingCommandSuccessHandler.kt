package io.holunda.camunda.datapool.sender

import org.axonframework.commandhandling.CommandResultMessage
import org.slf4j.Logger

/**
 * Success handler, logging.
 */
class LoggingCommandSuccessHandler(private val logger: Logger) : DataEntryCommandSuccessHandler {

  override fun apply(commandMessage: Any, commandResultMessage: CommandResultMessage<out Any?>) {
    if (logger.isDebugEnabled) {
      logger.debug("Successfully submitted command $commandMessage, $commandResultMessage")
    }
  }
}
