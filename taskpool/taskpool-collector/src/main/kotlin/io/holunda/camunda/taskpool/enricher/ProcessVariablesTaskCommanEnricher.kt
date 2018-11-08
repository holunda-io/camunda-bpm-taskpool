package io.holunda.camunda.taskpool.enricher

import io.holunda.camunda.taskpool.api.task.*
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables
import org.springframework.context.event.EventListener

class ProcessVariablesCreateCommandEnricher(runtimeService: RuntimeService, filter: ProcessVariablesFilter, correlator: ProcessVariablesCorrelator)
  : CreateCommandEnricher, ProcessVariablesTaskCommandEnricher(runtimeService, filter, correlator) {
  @EventListener(condition = "#command.enriched == false")
  override fun enrich(command: CreateTaskCommand): CreateTaskCommand = super.enrich(command)
}

class ProcessVariablesCompleteCommandEnricher(runtimeService: RuntimeService, filter: ProcessVariablesFilter, correlator: ProcessVariablesCorrelator)
  : CompleteCommandEnricher, ProcessVariablesTaskCommandEnricher(runtimeService, filter, correlator) {
  @EventListener(condition = "#command.enriched == false")
  override fun enrich(command: CompleteTaskCommand): CompleteTaskCommand = super.enrich(command)
}

class ProcessVariablesDeleteCommandEnricher(runtimeService: RuntimeService, filter: ProcessVariablesFilter, correlator: ProcessVariablesCorrelator)
  : DeleteCommandEnricher, ProcessVariablesTaskCommandEnricher(runtimeService, filter, correlator) {
  @EventListener(condition = "#command.enriched == false")
  override fun enrich(command: DeleteTaskCommand): DeleteTaskCommand = super.enrich(command)
}

class ProcessVariablesAssignCommandEnricher(runtimeService: RuntimeService, filter: ProcessVariablesFilter, correlator: ProcessVariablesCorrelator)
  : AssignCommandEnricher, ProcessVariablesTaskCommandEnricher(runtimeService, filter, correlator) {
  @EventListener(condition = "#command.enriched == false")
  override fun enrich(command: AssignTaskCommand): AssignTaskCommand = super.enrich(command)
}

/**
 * Enriches commands with process variables from the VariableContext.
 * @param runtimeService Camunda API to access the execution.
 * @param processVariablesFilter filter to whitelist or blacklist the variables which should be added to the task.
 */
open class ProcessVariablesTaskCommandEnricher(
  private val runtimeService: RuntimeService,
  private val processVariablesFilter: ProcessVariablesFilter,
  private val processVarriablesCorrelator: ProcessVariablesCorrelator
) {
  protected fun <T : EngineTaskCommand> enrich(command: T): T {

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
      processVarriablesCorrelator.correlateVariables(
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
