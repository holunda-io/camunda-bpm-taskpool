package io.holunda.polyflow.taskpool.collector.process.instance

import io.holunda.camunda.taskpool.api.process.instance.ProcessInstanceCommand
import io.holunda.polyflow.taskpool.sender.process.instance.ProcessInstanceCommandSender
import mu.KLogging
import org.springframework.context.event.EventListener

/**
 * Default process instance processor.
 */
class ProcessInstanceProcessor(
  private val processInstanceCommandSender: ProcessInstanceCommandSender
) {
  companion object : KLogging()

  /**
   * Reacts on incoming process instance commands.
   * @param command command about process instance to send.
   */
  @EventListener
  fun process(command: ProcessInstanceCommand) {
    if (logger.isTraceEnabled) {
      logger.trace { "COLLECTOR-006: Sending process instance command: $command" }
    }
    processInstanceCommandSender.send(command)
  }
}
