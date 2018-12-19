package io.holunda.camunda.taskpool.enricher

import io.holunda.camunda.taskpool.api.task.EnrichedEngineTaskCommand
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
  private val formService: FormService,
  private val processVariablesFilter: ProcessVariablesFilter,
  private val processVariablesCorrelator: ProcessVariablesCorrelator
) : VariablesEnricher {

  override fun <T : EnrichedEngineTaskCommand> enrich(command: T): T {

    // check if the execution exists.
    // stop enrichment if the execution doesn't exist anymore.
    runtimeService
      .createExecutionQuery()
      .executionId(command.sourceReference.executionId)
      .singleResult() ?: return command

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

fun VariableMap.putAllTyped(source: VariableMap) {
  source.keys.forEach {
    this.putValueTyped(it, source.getValueTyped(it))
  }
}

inline fun VariableMap.filterKeys(predicate: (String) -> Boolean): VariableMap {
  val result = Variables.createVariables()
  for (entry in this) {
    if (predicate(entry.key)) {
      result[entry.key] = entry.value
    }
  }
  return result
}
