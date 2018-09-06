package io.holunda.camunda.taskpool.enricher

import io.holunda.camunda.taskpool.api.task.*
import org.camunda.bpm.engine.RuntimeService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener

@Configuration
open class ProcessVariablesEnricher {

  @Bean
  open fun createEnricher(runtimeService: RuntimeService): CreateCommandEnricher = ProcessVariableCreateCommandEnricher(runtimeService)

  @Bean
  open fun assignEnricher(runtimeService: RuntimeService): AssignCommandEnricher = ProcessVariableAssignCommandEnricher(runtimeService)

  @Bean
  open fun deleteEnricher(runtimeService: RuntimeService): DeleteCommandEnricher = ProcessVariableDeleteCommandEnricher(runtimeService)

  @Bean
  open fun completeEnricher(runtimeService: RuntimeService): CompleteCommandEnricher = ProcessVariableCompleteCommandEnricher(runtimeService)
}

class ProcessVariableCreateCommandEnricher(runtimeService: RuntimeService) : CreateCommandEnricher, ProcessVariableEnricher(runtimeService) {
  @EventListener(condition = "#command.enriched == false")
  override fun enrich(command: CreateTaskCommand): CreateTaskCommand = super.enrich(command)
}

class ProcessVariableCompleteCommandEnricher(runtimeService: RuntimeService) : CompleteCommandEnricher, ProcessVariableEnricher(runtimeService) {
  @EventListener(condition = "#command.enriched == false")
  override fun enrich(command: CompleteTaskCommand): CompleteTaskCommand = super.enrich(command)
}

class ProcessVariableDeleteCommandEnricher(runtimeService: RuntimeService) : DeleteCommandEnricher, ProcessVariableEnricher(runtimeService) {
  @EventListener(condition = "#command.enriched == false")
  override fun enrich(command: DeleteTaskCommand): DeleteTaskCommand = super.enrich(command)
}

class ProcessVariableAssignCommandEnricher(runtimeService: RuntimeService) : AssignCommandEnricher, ProcessVariableEnricher(runtimeService) {
  @EventListener(condition = "#command.enriched == false")
  override fun enrich(command: AssignTaskCommand): AssignTaskCommand = super.enrich(command)
}

open class ProcessVariableEnricher(private val runtimeService: RuntimeService) {
  protected fun <T : TaskCommand> enrich(command: T): T {
    val variables: Map<String, Any> =
      when {
          command.processReference != null -> runtimeService.getVariables(command.processReference!!.executionId)
          command.caseReference != null -> runtimeService.getVariables(command.caseReference!!.caseExecutionId)
          else -> mapOf()
      }
    command.payload.putAll(variables)
    command.enriched = true
    return command
  }
}

