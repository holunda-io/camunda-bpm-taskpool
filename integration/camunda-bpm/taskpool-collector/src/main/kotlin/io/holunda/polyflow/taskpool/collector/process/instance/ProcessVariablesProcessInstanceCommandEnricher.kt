package io.holunda.polyflow.taskpool.collector.process.instance

import io.holunda.camunda.taskpool.api.process.instance.ProcessInstanceCommand
import io.holunda.polyflow.taskpool.collector.task.enricher.ProcessVariablesCorrelator
import io.holunda.polyflow.taskpool.collector.task.enricher.ProcessVariablesFilter
import io.holunda.polyflow.taskpool.putAllTyped

/**
 * Enriches process-instance lifecycle commands with configured process variables and global correlations.
 */
class ProcessVariablesProcessInstanceCommandEnricher(
  private val processVariablesFilter: ProcessVariablesFilter,
  private val processVariablesCorrelator: ProcessVariablesCorrelator,
  private val processInstanceVariableLoader: ProcessInstanceVariableLoader,
) : ProcessInstanceVariablesEnricher {

  override fun <T : ProcessInstanceCommand> enrich(command: T): T {
    val definitionKey = command.sourceReference.definitionKey
    val variables = if (processVariablesFilter.hasRestrictionsFor(definitionKey)) {
      val availableVariables = processInstanceVariableLoader.getTypedVariables(command, deserializeValues = false)
      val payloadVariables = processVariablesFilter.filterVariables(definitionKey, availableVariables)
      val variableNames = payloadVariables.keys + processVariablesCorrelator.variableNamesFor(definitionKey)
      processInstanceVariableLoader.getTypedVariables(command, variableNames)
    } else {
      processInstanceVariableLoader.getTypedVariables(command)
    }

    command.payload.putAllTyped(processVariablesFilter.filterVariables(definitionKey, variables))
    command.correlations.putAllTyped(processVariablesCorrelator.correlateVariables(definitionKey, variables))
    command.enriched = true
    return command
  }
}
