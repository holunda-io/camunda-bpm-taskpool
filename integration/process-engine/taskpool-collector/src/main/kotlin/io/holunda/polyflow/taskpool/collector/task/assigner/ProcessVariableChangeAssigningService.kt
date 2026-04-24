package io.holunda.polyflow.taskpool.collector.task.assigner

import io.holunda.camunda.taskpool.api.task.AddCandidateUsersCommand
import io.holunda.camunda.taskpool.api.task.AssignTaskCommand
import io.holunda.camunda.taskpool.api.task.EngineTaskCommand
import io.holunda.camunda.taskpool.api.task.SourceReference
import io.holunda.polyflow.taskpool.sender.process.variable.CreateSingleProcessVariableCommand
import io.holunda.polyflow.taskpool.sender.process.variable.UpdateSingleProcessVariableCommand
import org.camunda.bpm.engine.TaskService
import org.springframework.context.event.EventListener

/**
 * This service bridges the gap between changes in process variables and change of assignments of existing user tasks.
 * It subscribes to changes to variables configures as assignment process variables and reacts to those changes
 * emitting task assignment update events if the underlying process instance is waiting in a user task.
 *
 * In order to work properly, the collector needs to activate variable collection (even if the sender is disabled).
 */
class ProcessVariableChangeAssigningService(
  private val taskService: TaskService,
  private val mapping: ProcessVariableTaskAssignerMapping
) {

  /**
   * React on new variables created.
   */
  @EventListener
  fun on(command: CreateSingleProcessVariableCommand): EngineTaskCommand? {
    // only relevant if waiting in a user task
    val taskId = getTaskId(command.sourceReference) ?: return null
    return when (command.variableName) {
      mapping.assignee -> {
        AssignTaskCommand(
          id = taskId,
          assignee = command.value.value.asStringValue()
        )
      }

      mapping.candidateUsers -> {
        if (command.value.value != null) {
          AddCandidateUsersCommand(
            id = taskId,
            candidateUsers = command.value.value.asSetValue()
          )
        } else {
          null
        }
      }

      mapping.candidateGroups -> {
        null
      }

      else -> null
    }
  }

  /**
   * React on variables updates.
   */
  @EventListener
  fun on(command: UpdateSingleProcessVariableCommand): EngineTaskCommand? {
    val taskId = getTaskId(command.sourceReference) ?: return null
    return when (command.variableName) {
      mapping.assignee -> {
        AssignTaskCommand(
          id = taskId,
          assignee = command.value.value.asStringValue()
        )
      }

      mapping.candidateUsers -> {
        if (command.value.value != null) {
          AddCandidateUsersCommand(
            id = taskId,
            candidateUsers = command.value.value.asSetValue()
          )
        } else {
          null
        }
      }

      mapping.candidateGroups -> {
        null
      }

      else -> null
    }
  }


  private fun getTaskId(sourceReference: SourceReference): String? {
    return taskService.createTaskQuery().executionId(sourceReference.executionId).singleResult()?.id
  }
}
