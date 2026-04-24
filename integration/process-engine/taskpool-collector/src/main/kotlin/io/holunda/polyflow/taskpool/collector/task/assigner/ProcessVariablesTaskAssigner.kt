package io.holunda.polyflow.taskpool.collector.task.assigner

import io.holunda.camunda.taskpool.api.task.CreateTaskCommand
import io.holunda.camunda.taskpool.api.task.EngineTaskCommand
import io.holunda.polyflow.taskpool.collector.task.TaskAssigner
import io.holunda.polyflow.taskpool.collector.task.TaskVariableLoader

/**
 * Task assigner retrieving assignment information from process variables.
 */
class ProcessVariablesTaskAssigner(
  private val taskVariableLoader: TaskVariableLoader,
  private val processVariableTaskAssignerMapping: ProcessVariableTaskAssignerMapping
) : TaskAssigner {
  override fun setAssignment(command: EngineTaskCommand): EngineTaskCommand =
    when (command) {
      is CreateTaskCommand -> processVariableTaskAssignerMapping
        .loadAssignmentFromVariables(variables = taskVariableLoader.getTypeVariables(command))
        .let { assignment ->
          command.let {
            if (assignment.assignee != null) {
              it.copy(assignee = assignment.assignee)
            } else {
              it
            }
          }.let {
            if (assignment.candidateUsers.isNotEmpty()) {
              it.copy(candidateUsers = assignment.candidateUsers)
            } else {
              it
            }
          }.let {
            if (assignment.candidateGroups.isNotEmpty()) {
              it.copy(candidateGroups = assignment.candidateGroups)
            } else {
              it
            }
          }
        }

      else -> command
    }

}
