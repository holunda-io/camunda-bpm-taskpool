package io.holunda.polyflow.taskpool.collector.task

import io.holunda.camunda.taskpool.api.task.TaskIdentityWithPayloadAndCorrelations

/**
 * Enriches commands with payload.
 */
interface VariablesEnricher {
  /**
   * Enriches command <T> with payload and correlations.
   */
  fun <T : TaskIdentityWithPayloadAndCorrelations> enrich(command: T): T
}
