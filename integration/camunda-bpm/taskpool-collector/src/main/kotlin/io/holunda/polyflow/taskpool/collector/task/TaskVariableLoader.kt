package io.holunda.polyflow.taskpool.collector.task

import io.github.oshai.kotlinlogging.KotlinLogging
import io.holunda.camunda.taskpool.api.task.*
import io.holunda.polyflow.taskpool.callInProcessEngineContext
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.TaskService
import org.camunda.bpm.engine.impl.db.entitymanager.DbEntityManager
import org.camunda.bpm.engine.impl.interceptor.CommandContext
import org.camunda.bpm.engine.impl.interceptor.CommandContextListener
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables

private val logger = KotlinLogging.logger {}

/**
 * Facility to load variables in a command context.
 * @param commandExecutor Camunda API executor.
 * @param taskService Camunda API to access tasks.
 * @param runtimeService Camunda API to access the execution.
 */
class TaskVariableLoader(
  private val runtimeService: RuntimeService,
  private val taskService: TaskService,
  private val commandExecutor: CommandExecutor,
) {

  /**
   * Retrieves typed variables from the context of a command.
   * @param command command context.
   * @param deserializeValues whether variable values should be deserialized.
   * @return variable map.
   */
  fun <T : TaskIdentity> getTypeVariables(command: T, deserializeValues: Boolean = true): VariableMap =
    loadTypeVariables(command, variableNames = null, deserializeValues = deserializeValues)

  /**
   * Retrieves selected typed variables from the context of a command.
   *
   * @param command command context.
   * @param variableNames names of variables to retrieve.
   * @param deserializeValues whether variable values should be deserialized.
   * @return variable map.
   */
  fun <T : TaskIdentity> getTypeVariables(
    command: T,
    variableNames: Collection<String>,
    deserializeValues: Boolean = true
  ): VariableMap {
    if (variableNames.isEmpty()) return Variables.createVariables()
    return loadTypeVariables(command, variableNames, deserializeValues)
  }

  private fun <T : TaskIdentity> loadTypeVariables(
    command: T,
    variableNames: Collection<String>?,
    deserializeValues: Boolean
  ): VariableMap {
    return if (command.isHistoric()) {
      // Task updated
      // This is a historic command which is processed from a command context listener on command context close. Accessing variables at this point will try to add
      // another command context listener (some camunda feature about updating mutable variables without explicitly saving them), which in turn causes a
      // ConcurrentModificationException.
      // We work around this by opening a new context.
      // Caution: This will only work if the task is already flushed to the database, e.g. if it was created in a previous transaction.
      // It will also see only the state of the variables that is flushed to the database
      callInProcessEngineContext(newContext = true) {
        commandExecutor.execute { innerContext ->
          val task = taskService.createTaskQuery().taskId(command.id).singleResult()
          if (task != null) {
            readTaskVariables(command.id, variableNames, deserializeValues)
          } else {
            val execution = runtimeService.createExecutionQuery().executionId(command.sourceReference.executionId).singleResult()
            if (execution != null) {
              readExecutionVariables(command.sourceReference.executionId, variableNames, deserializeValues)
            } else {
              logger.debug { "ENRICHER-004: Could not enrich variables from running execution ${command.sourceReference.executionId}, since it doesn't exist (anymore)." }
              Variables.createVariables()
            }
          }.also {
            innerContext.registerCommandContextListener(object : CommandContextListener {
              override fun onCommandContextClose(commandContext: CommandContext) {
                // Remove all cached entities from the inner context because we don't want to accidentally flush any changes that could cause
                // OptimisticLockingExceptions in the outer context.
                // (We don't change any variables but there are situations where _reading_ a variable also causes a change, e.g. when the serialized form of a
                // complex variable has changed because properties have been added since creation of the variable. Another example: A variable containing a Set
                // has been created and serialized from a LinkedHashSet with a specific order, but is deserialized to a HashSet with a different iteration order.
                // When this is serialized again, the serialized form will look different because the order has changed.
                (commandContext.sessions[DbEntityManager::class.java] as? DbEntityManager)?.dbEntityCache?.apply { cachedEntities.forEach { remove(it) } }
              }

              override fun onCommandFailed(commandContext: CommandContext, t: Throwable) {
              }
            })
          }
        }
      }
    } else {
      // Create task
      readTaskVariables(command.id, variableNames, deserializeValues)
    }
  }

  private fun readTaskVariables(taskId: String, variableNames: Collection<String>?, deserializeValues: Boolean): VariableMap =
    if (variableNames == null) {
      taskService.getVariablesTyped(taskId, deserializeValues)
    } else {
      taskService.getVariablesTyped(taskId, variableNames, deserializeValues)
    }

  private fun readExecutionVariables(executionId: String, variableNames: Collection<String>?, deserializeValues: Boolean): VariableMap =
    if (variableNames == null) {
      runtimeService.getVariablesTyped(executionId, deserializeValues)
    } else {
      runtimeService.getVariablesTyped(executionId, variableNames, deserializeValues)
    }
}

/**
 * Checks if the command is created from a historic Camunda event.
 */
fun Any.isHistoric(): Boolean =
  when (this) {
    is CreateTaskCommand, is DeleteTaskCommand, is CompleteTaskCommand, is AssignTaskCommand, is UpdateAttributeTaskCommand -> false
    is UpdateAttributesHistoricTaskCommand, is UpdateAssignmentTaskCommand -> true
    else -> throw IllegalArgumentException("Unexpected command received: '$this'")
  }
