package io.holunda.camunda.taskpool.collector.task.enricher

import io.holunda.camunda.taskpool.api.task.TaskIdentityWithPayloadAndCorrelations
import io.holunda.camunda.taskpool.collector.task.VariablesEnricher


/**
 * Empty implementation without any enrichment. Used for compliance of the commands.
 */
class EmptyTaskCommandEnricher : VariablesEnricher {
  override fun <T : TaskIdentityWithPayloadAndCorrelations> enrich(command: T): T = command.apply { enriched = true }
}
