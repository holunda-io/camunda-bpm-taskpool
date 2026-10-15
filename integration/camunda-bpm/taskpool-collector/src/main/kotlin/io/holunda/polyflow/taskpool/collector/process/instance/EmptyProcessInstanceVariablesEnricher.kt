package io.holunda.polyflow.taskpool.collector.process.instance

import io.holunda.camunda.taskpool.api.process.instance.ProcessInstanceCommand

/**
 * Marks process-instance commands as deliberately enriched without adding variables or correlations.
 */
class EmptyProcessInstanceVariablesEnricher : ProcessInstanceVariablesEnricher {
  override fun <T : ProcessInstanceCommand> enrich(command: T): T = command.apply { enriched = true }
}
