package io.holunda.polyflow.taskpool.collector.task.assigner

import io.holunda.camunda.taskpool.api.task.EngineTaskCommand
import io.holunda.polyflow.taskpool.collector.task.TaskAssigner

/**
 * No-op task assigner changing nothing.
 */
class EmptyTaskAssigner : TaskAssigner {
  override fun setAssignment(command: EngineTaskCommand): EngineTaskCommand = command
}
