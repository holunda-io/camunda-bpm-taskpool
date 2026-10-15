package io.holunda.polyflow.taskpool.collector.process.definition

import io.holunda.camunda.taskpool.api.process.definition.ProcessDefinitionCommand

/**
 * Enriches a process-definition lifecycle command before it is sent to Taskpool Core.
 */
interface ProcessDefinitionCommandEnricher {

  /**
   * Adds typed payload data to a process-definition command.
   *
   * @param command command to enrich.
   * @return enriched command.
   */
  fun <T : ProcessDefinitionCommand> enrich(command: T): T
}
