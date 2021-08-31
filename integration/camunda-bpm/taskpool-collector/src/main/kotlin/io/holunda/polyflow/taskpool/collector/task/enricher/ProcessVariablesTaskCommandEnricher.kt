package io.holunda.polyflow.taskpool.collector.task.enricher

import io.holunda.camunda.taskpool.api.task.*
import io.holunda.polyflow.taskpool.callInProcessEngineContext
import io.holunda.polyflow.taskpool.putAllTyped
import io.holunda.polyflow.taskpool.collector.task.VariablesEnricher
import mu.KLogging
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.TaskService
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables.createVariables

/**
 * Enriches commands with process variables.
 * @param runtimeService Camunda API to access the execution.
 * @param processVariablesFilter filter to whitelist or blacklist the variables which should be added to the task.
 */
open class ProcessVariablesTaskCommandEnricher(
  private val runtimeService: RuntimeService,
  private val taskService: TaskService,
  private val processVariablesFilter: ProcessVariablesFilter,
  private val processVariablesCorrelator: ProcessVariablesCorrelator,
) : VariablesEnricher {

  companion object : KLogging()


  /**
   * Retrieves typed variables to enrich current command.
   * @param command command to enrich.
   * @return variable map.
   */
  open fun <T : TaskIdentityWithPayloadAndCorrelations> getTypeVariables(command: T): VariableMap {
    return if (command.isHistoric()) {
      // Task updated
      // This is a historic command which is processed from a command context listener on command context close. Accessing variables at this point will try to add
      // another command context listener (some camunda feature about updating mutable variables without explicitly saving them), which in turn causes a
      // ConcurrentModificationException.
      // We work around this by opening a new context.
      // Caution: This will only work if the task is already flushed to the database, e.g. if it was created in a previous transaction.
      // It will also see only the state of the variables that is flushed to the database
      callInProcessEngineContext(newContext = true) {
        val task = taskService.createTaskQuery().taskId(command.id).singleResult()
        if (task != null) {
          taskService.getVariablesTyped(command.id)
        } else {
          val execution = runtimeService.createExecutionQuery().executionId(command.sourceReference.executionId).singleResult()
          if (execution != null) {
            runtimeService.getVariablesTyped(command.sourceReference.executionId)
          } else {
            logger.debug { "ENRICHER-004: Could not enrich variables from running execution ${command.sourceReference.executionId}, since it doesn't exist (anymore)." }
            createVariables()
          }
        }
      }
    } else {
      // Create task
      taskService.getVariablesTyped(command.id)
    }
  }

  override fun <T : TaskIdentityWithPayloadAndCorrelations> enrich(command: T): T {

    // load variables typed
    val variablesTyped = getTypeVariables(command)

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





