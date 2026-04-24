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

    // load variables typed
    val variablesTyped = taskVariableLoader.getTypeVariables(command)

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

