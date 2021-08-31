package io.holunda.polyflow.taskpool.sender.gateway

import org.axonframework.commandhandling.CommandResultMessage
import org.slf4j.Logger

/**
 * Error handler, logging the error.
 */
open class LoggingTaskCommandErrorHandler(private val logger: Logger) : CommandErrorHandler {

  override fun apply(commandMessage: Any, commandResultMessage: CommandResultMessage<out Any?>) {
    logger.error("SENDER-003: Sending command $commandMessage resulted in error", commandResultMessage.exceptionResult())
  }
}
