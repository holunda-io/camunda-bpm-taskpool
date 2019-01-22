package io.holunda.camunda.taskpool.enricher

import io.holunda.camunda.taskpool.api.task.TaskIdentityWithPayloadAndCorrelations


/**
 * Empty implementation without eny enrichment. Used for compliance of the commands.
 */
class EmptyTaskCommandEnricher : VariablesEnricher {
  override fun <T : TaskIdentityWithPayloadAndCorrelations> enrich(command: T): T = command.apply { enriched = true }
}
