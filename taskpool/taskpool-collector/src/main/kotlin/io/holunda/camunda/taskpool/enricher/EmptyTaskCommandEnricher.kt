package io.holunda.camunda.taskpool.enricher

import io.holunda.camunda.taskpool.api.task.EnrichedEngineTaskCommand

/**
 * Enriches commands with payload.
 */
interface VariablesEnricher {
  fun <T : EnrichedEngineTaskCommand> enrich(command: T): T
}

/**
 * Empty implementation without eny enrichment. Used for compliance of the commands.
 */
class EmptyEnricher : VariablesEnricher {
  override fun <T : EnrichedEngineTaskCommand> enrich(command: T): T = command.apply { enriched = true }
}
