package io.holunda.camunda.taskpool.enricher

import io.holunda.camunda.taskpool.api.task.TaskIdentityWithPayloadAndCorrelations

/**
 * Enriches commands with payload.
 */
interface VariablesEnricher {
  fun <T : TaskIdentityWithPayloadAndCorrelations> enrich(command: T): T
}
