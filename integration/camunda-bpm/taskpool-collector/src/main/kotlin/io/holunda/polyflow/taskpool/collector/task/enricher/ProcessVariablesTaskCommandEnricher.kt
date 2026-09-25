package io.holunda.polyflow.taskpool.collector.task.enricher

import io.holunda.camunda.taskpool.api.task.TaskIdentityWithPayloadAndCorrelations
import io.holunda.polyflow.taskpool.collector.task.TaskVariableLoader
import io.holunda.polyflow.taskpool.collector.task.VariablesEnricher
import io.holunda.polyflow.taskpool.putAllTyped

/**
 * Enriches commands with process variables.
 * @param processVariablesFilter filter to whitelist or blacklist the variables which should be added to the task.
 */
open class ProcessVariablesTaskCommandEnricher(
  private val processVariablesFilter: ProcessVariablesFilter,
  private val processVariablesCorrelator: ProcessVariablesCorrelator,
  private val taskVariableLoader: TaskVariableLoader
) : VariablesEnricher {

  override fun <T : TaskIdentityWithPayloadAndCorrelations> enrich(command: T): T {

    val variablesTyped = if (processVariablesFilter.hasRestrictionsFor(command.sourceReference.definitionKey, command.taskDefinitionKey)) {
      // Load variable names without deserializing values. This prevents excluded
      // complex variables from requiring their application classes on the engine.
      val availableVariables = taskVariableLoader.getTypeVariables(command, deserializeValues = false)
      val payloadVariables = processVariablesFilter.filterVariables(
        command.sourceReference.definitionKey,
        command.taskDefinitionKey,
        availableVariables
      )
      val variableNames = payloadVariables.keys + processVariablesCorrelator.variableNamesFor(
        command.sourceReference.definitionKey,
        command.taskDefinitionKey
      )

      // Deserialize only payload and correlation variables.
      taskVariableLoader.getTypeVariables(command, variableNames)
    } else {
      // Preserve the single read when no filter restricts the payload.
      taskVariableLoader.getTypeVariables(command)
    }

    // Payload enrichment
    command.payload.putAllTyped(
      processVariablesFilter.filterVariables(
        command.sourceReference.definitionKey,
        command.taskDefinitionKey,
        variablesTyped
      )
    )

    // Correlations
    command.correlations.putAllTyped(
      processVariablesCorrelator.correlateVariables(
        command.sourceReference.definitionKey,
        command.taskDefinitionKey,
        variablesTyped
      )
    )

    // Mark as enriched
    command.enriched = true
    return command
  }
}
