package io.holunda.camunda.taskpool.enricher

import io.holunda.camunda.taskpool.api.task.TaskIdentityWithPayloadAndCorrelations
import io.holunda.camunda.taskpool.api.task.WithFormKey
import org.camunda.bpm.engine.FormService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables

/**
 * Enriches commands with process variables from the VariableContext.
 * @param runtimeService Camunda API to access the execution.
 * @param processVariablesFilter filter to whitelist or blacklist the variables which should be added to the task.
 */
open class ProcessVariablesTaskCommandEnricher(
  private val runtimeService: RuntimeService,
  private val processVariablesFilter: ProcessVariablesFilter,
  private val processVariablesCorrelator: ProcessVariablesCorrelator
) : VariablesEnricher {

  override fun <T : TaskIdentityWithPayloadAndCorrelations> enrich(command: T): T {

    // Payload enrichment
    command.payload.putAllTyped(
      processVariablesFilter.filterVariables(
        command.sourceReference.definitionKey,
        command.taskDefinitionKey,
        runtimeService.getVariablesTyped(command.sourceReference.executionId)
      )
    )

    // Correlations
    command.correlations.putAllTyped(
      processVariablesCorrelator.correlateVariables(
        command.sourceReference.definitionKey,
        command.taskDefinitionKey,
        runtimeService.getVariablesTyped(command.sourceReference.executionId)
      )
    )

    // Mark as enriched
    command.enriched = true
    return command
  }
}

