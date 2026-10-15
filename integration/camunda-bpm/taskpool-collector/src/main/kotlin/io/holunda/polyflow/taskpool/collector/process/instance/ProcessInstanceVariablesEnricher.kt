package io.holunda.polyflow.taskpool.collector.process.instance

import io.holunda.camunda.taskpool.api.process.instance.ProcessInstanceCommand

/**
 * Enriches a process-instance lifecycle command before it is sent to Taskpool Core.
 */
interface ProcessInstanceVariablesEnricher {
  /**
   * Adds the configured payload and correlations to a lifecycle command.
   *
   * @param command lifecycle command to enrich.
   * @return the enriched command.
   */
  fun <T : ProcessInstanceCommand> enrich(command: T): T
}
