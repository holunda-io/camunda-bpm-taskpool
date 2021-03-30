package io.holunda.camunda.taskpool.enricher

import io.holunda.camunda.taskpool.api.task.*
import io.holunda.camunda.taskpool.callInProcessEngineContext
import mu.KLogging
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.variable.Variables.createVariables

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

  companion object : KLogging()

  override fun <T : TaskIdentityWithPayloadAndCorrelations> enrich(command: T): T {

    val variablesTyped = callInProcessEngineContext(command.isHistoric()) {
      val execution = runtimeService.createExecutionQuery().executionId(command.sourceReference.executionId).singleResult()
      if (execution != null) {
        runtimeService.getVariablesTyped(command.sourceReference.executionId)
      } else {
        logger.debug { "ENRICHER-004: Could not enrich variables from running execution ${command.sourceReference.executionId}, since it doesn't exist (anymore)." }
        createVariables()
      }
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





