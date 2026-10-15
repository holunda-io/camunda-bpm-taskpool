package io.holunda.polyflow.taskpool.collector.process.definition

import io.github.oshai.kotlinlogging.KotlinLogging
import io.holunda.camunda.taskpool.api.process.definition.ProcessDefinitionCommand
import io.holunda.polyflow.taskpool.sender.process.definition.ProcessDefinitionCommandSender
import org.springframework.context.event.EventListener

private val logger = KotlinLogging.logger {}

/**
 * Processes commands sent via Spring Eventing and delegates them to taskpool command sender.
 */
class ProcessDefinitionProcessor(
  private val processDefinitionCommandSender: ProcessDefinitionCommandSender,
  private val enricher: ProcessDefinitionCommandEnricher,
) {

  /**
   * Receives the process definition command and pass it over to the sender.
   * @param command process definition command, delivered by Spring eventing.
   */
  @EventListener
  fun process(command: ProcessDefinitionCommand) {
    val enrichedCommand = enricher.enrich(command)
    if (logger.isTraceEnabled()) {
      logger.trace { "COLLECTOR-005: Sending process definition command: $enrichedCommand" }
    }
    processDefinitionCommandSender.send(enrichedCommand)
  }
}
