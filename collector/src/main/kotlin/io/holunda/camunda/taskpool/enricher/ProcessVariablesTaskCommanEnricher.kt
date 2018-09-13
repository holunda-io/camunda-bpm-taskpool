package io.holunda.camunda.taskpool.enricher

import io.holunda.camunda.taskpool.api.task.*
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables
import org.springframework.context.event.EventListener

class ProcessVariablesCreateCommandEnricher(runtimeService: RuntimeService) : CreateCommandEnricher, ProcessVariablesTaskCommandEnricher(runtimeService) {
  @EventListener(condition = "#command.enriched == false")
  override fun enrich(command: CreateTaskCommand): CreateTaskCommand = super.enrich(command)
}

class ProcessVariablesCompleteCommandEnricher(runtimeService: RuntimeService) : CompleteCommandEnricher, ProcessVariablesTaskCommandEnricher(runtimeService) {
  @EventListener(condition = "#command.enriched == false")
  override fun enrich(command: CompleteTaskCommand): CompleteTaskCommand = super.enrich(command)
}

class ProcessVariablesDeleteCommandEnricher(runtimeService: RuntimeService) : DeleteCommandEnricher, ProcessVariablesTaskCommandEnricher(runtimeService) {
  @EventListener(condition = "#command.enriched == false")
  override fun enrich(command: DeleteTaskCommand): DeleteTaskCommand = super.enrich(command)
}

class ProcessVariablesAssignCommandEnricher(runtimeService: RuntimeService) : AssignCommandEnricher, ProcessVariablesTaskCommandEnricher(runtimeService) {
  @EventListener(condition = "#command.enriched == false")
  override fun enrich(command: AssignTaskCommand): AssignTaskCommand = super.enrich(command)
}

open class ProcessVariablesTaskCommandEnricher(private val runtimeService: RuntimeService) {
  protected fun <T : TaskCommand> enrich(command: T): T {
    val variables: VariableMap =
      when {
        command.processReference != null -> runtimeService.getVariablesTyped(command.processReference!!.executionId)
        command.caseReference != null -> runtimeService.getVariablesTyped(command.caseReference!!.caseExecutionId)
        else -> Variables.createVariables()
      }

    command.payload.putAllTyped(variables)
    command.enriched = true

    return command
  }
}

fun VariableMap.putAllTyped(source: VariableMap) {
  source.keys.forEach {
    this.putValueTyped(it, source.getValueTyped(it))
  }
}
