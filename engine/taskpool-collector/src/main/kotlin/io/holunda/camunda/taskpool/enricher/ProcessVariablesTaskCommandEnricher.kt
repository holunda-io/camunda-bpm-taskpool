package io.holunda.camunda.taskpool.enricher

import io.holunda.camunda.taskpool.api.task.*
import io.holunda.camunda.taskpool.callInProcessEngineContext
import org.camunda.bpm.engine.RuntimeService

/**
 * Enriches commands with process variables.
 * @param runtimeService Camunda API to access the execution.
 * @param processVariablesFilter filter to whitelist or blacklist the variables which should be added to the task.
 */
open class ProcessVariablesTaskCommandEnricher(
  private val runtimeService: RuntimeService,
  private val processVariablesFilter: ProcessVariablesFilter,
  private val processVariablesCorrelator: ProcessVariablesCorrelator,
) : VariablesEnricher {

  override fun <T : TaskIdentityWithPayloadAndCorrelations> enrich(command: T): T {

    val variablesTyped = callInProcessEngineContext(command.isHistoric()) {
      runtimeService.getVariablesTyped(command.sourceReference.executionId)
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

/**
 * Checks if the command is created from a historic Camunda event.
 */
fun Any.isHistoric(): Boolean =
  when (this) {
    is CreateTaskCommand, is DeleteTaskCommand, is CompleteTaskCommand, is AssignTaskCommand -> false
    is UpdateAttributeTaskCommand, is UpdateAssignmentTaskCommand -> true
    else -> throw IllegalArgumentException("Unexpected command received: '$this'")
  }





