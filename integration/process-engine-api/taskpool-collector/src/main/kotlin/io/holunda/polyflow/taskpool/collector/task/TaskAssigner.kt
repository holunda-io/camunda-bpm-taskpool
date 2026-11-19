package io.holunda.polyflow.taskpool.collector.task

import io.holunda.camunda.taskpool.api.task.EngineTaskCommand

/**
 * Hook for assignment changes.
 */
interface TaskAssigner {
  /**
   * Sets assignment of a task command.
   * @param command task command
   * @return command with modified assignment information.
   */
  fun setAssignment(command: EngineTaskCommand): EngineTaskCommand
}
