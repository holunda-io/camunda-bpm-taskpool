package io.holunda.polyflow.taskpool.collector.task.enricher

import io.holunda.camunda.taskpool.api.task.*
import io.holunda.polyflow.taskpool.callInProcessEngineContext
import io.holunda.polyflow.taskpool.collector.task.TaskVariableLoader
import io.holunda.polyflow.taskpool.collector.task.VariablesEnricher
import io.holunda.polyflow.taskpool.putAllTyped
import mu.KLogging
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.TaskService
import org.camunda.bpm.engine.impl.db.entitymanager.DbEntityManager
import org.camunda.bpm.engine.impl.interceptor.CommandContext
import org.camunda.bpm.engine.impl.interceptor.CommandContextListener
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables.createVariables

/**
 * Enriches commands with process variables.
 * @param processVariablesFilter filter to whitelist or blacklist the variables which should be added to the task.
 */
open class ProcessVariablesTaskCommandEnricher(
  private val processVariablesFilter: ProcessVariablesFilter,
  private val processVariablesCorrelator: ProcessVariablesCorrelator,
  private val taskVariableLoader: TaskVariableLoader
) : VariablesEnricher {

  companion object : KLogging()


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

