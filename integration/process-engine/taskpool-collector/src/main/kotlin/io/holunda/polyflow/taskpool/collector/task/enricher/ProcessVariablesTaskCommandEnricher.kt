package io.holunda.polyflow.taskpool.collector.task.enricher

import io.holunda.camunda.taskpool.api.task.TaskIdentityWithPayloadAndCorrelations
import io.holunda.polyflow.taskpool.collector.task.VariablesEnricher
import io.holunda.polyflow.taskpool.putAllTyped

/**
 * Enriches commands with process variables.
 * @param processVariablesFilter filter to whitelist or blacklist the variables which should be added to the task.
 */
open class ProcessVariablesTaskCommandEnricher(
  private val processVariablesFilter: ProcessVariablesFilter,
  private val processVariablesCorrelator: ProcessVariablesCorrelator,
) : VariablesEnricher {

  override fun <T : TaskIdentityWithPayloadAndCorrelations> enrich(command: T): T {


    // Payload enrichment

    command.payload.putAllTyped(
      processVariablesFilter.filterVariables(
        command.sourceReference.definitionKey,
        command.taskDefinitionKey,
        command.payload
      )
    )

    // Correlations
    command.correlations.putAllTyped(
      processVariablesCorrelator.correlateVariables(
        command.sourceReference.definitionKey,
        command.taskDefinitionKey,
        command.payload
      )
    )

    // Mark as enriched
    command.enriched = true
    return command
  }
}

