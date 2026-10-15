package io.holunda.polyflow.taskpool.collector.process.instance

import io.github.oshai.kotlinlogging.KotlinLogging
import io.holunda.camunda.taskpool.api.process.instance.ProcessInstanceCommand
import io.holunda.polyflow.taskpool.sender.process.instance.ProcessInstanceCommandSender
import org.springframework.context.event.EventListener

private val logger = KotlinLogging.logger {}

/**
 * Default process instance processor.
 */
class ProcessInstanceProcessor(
  private val processInstanceCommandSender: ProcessInstanceCommandSender,
  private val enricher: ProcessInstanceVariablesEnricher,
) {

  /**
   * Reacts on incoming process instance commands.
   * @param command command about process instance to send.
   */
  @EventListener
  fun process(command: ProcessInstanceCommand) {
    val enrichedCommand = enricher.enrich(command)
    if (logger.isTraceEnabled()) {
      logger.trace { "COLLECTOR-006: Sending process instance command: $enrichedCommand" }
    }
    processInstanceCommandSender.send(enrichedCommand)
  }
}
