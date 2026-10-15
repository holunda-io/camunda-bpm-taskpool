package io.holunda.polyflow.taskpool.collector.process.definition

import io.holunda.camunda.taskpool.api.process.definition.ProcessDefinitionCommand
import io.holunda.polyflow.taskpool.collector.ProcessDefinitionAttributeProperties

/**
 * Enriches process-definition commands with global and process-key-specific configured attributes.
 */
class PropertyBasedProcessDefinitionEnricher(
  attributes: List<ProcessDefinitionAttributeProperties>,
) : ProcessDefinitionCommandEnricher {

  private val globalAttributes = attributes.filter { it.processDefinitionKey == null }.flatMap { it.values.entries }.associate { it.toPair() }
  private val processAttributes = attributes.filter { it.processDefinitionKey != null }.associate { requireNotNull(it.processDefinitionKey) to it.values }

  /**
   * Adds global attributes followed by attributes scoped to the command's process-definition key.
   *
   * @param command command to enrich.
   * @return enriched command.
   */
  override fun <T : ProcessDefinitionCommand> enrich(command: T): T = command.apply {
    payload.putAll(globalAttributes)
    processDefinitionKey?.let { payload.putAll(processAttributes[it].orEmpty()) }
  }
}
