package io.holunda.camunda.taskpool.enricher

import io.holunda.camunda.taskpool.api.task.*
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables
import org.springframework.context.event.EventListener

class ProcessVariablesCreateCommandEnricher(runtimeService: RuntimeService, filter: ProcessVariablesFilter)
  : CreateCommandEnricher, ProcessVariablesTaskCommandEnricher(runtimeService, filter) {
  @EventListener(condition = "#command.enriched == false")
  override fun enrich(command: CreateTaskCommand): CreateTaskCommand = super.enrich(command)
}

class ProcessVariablesCompleteCommandEnricher(runtimeService: RuntimeService, filter: ProcessVariablesFilter)
  : CompleteCommandEnricher, ProcessVariablesTaskCommandEnricher(runtimeService, filter) {
  @EventListener(condition = "#command.enriched == false")
  override fun enrich(command: CompleteTaskCommand): CompleteTaskCommand = super.enrich(command)
}

class ProcessVariablesDeleteCommandEnricher(runtimeService: RuntimeService, filter: ProcessVariablesFilter)
  : DeleteCommandEnricher, ProcessVariablesTaskCommandEnricher(runtimeService, filter) {
  @EventListener(condition = "#command.enriched == false")
  override fun enrich(command: DeleteTaskCommand): DeleteTaskCommand = super.enrich(command)
}

class ProcessVariablesAssignCommandEnricher(runtimeService: RuntimeService, filter: ProcessVariablesFilter)
  : AssignCommandEnricher, ProcessVariablesTaskCommandEnricher(runtimeService, filter) {
  @EventListener(condition = "#command.enriched == false")
  override fun enrich(command: AssignTaskCommand): AssignTaskCommand = super.enrich(command)
}

open class ProcessVariablesTaskCommandEnricher(
  private val runtimeService: RuntimeService,
  private val processVariablesFilter: ProcessVariablesFilter
) {
  protected fun <T : TaskCommand> enrich(command: T): T {
    command.payload.putAllTyped(
      processVariablesFilter.filterVariables(
        command.sourceReference.definitionKey,
        command.taskDefinitionKey,
        runtimeService.getVariablesTyped(command.sourceReference.executionId)
      )
    )
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
