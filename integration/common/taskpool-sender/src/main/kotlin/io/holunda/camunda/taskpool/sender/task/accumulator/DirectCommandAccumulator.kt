package io.holunda.camunda.taskpool.sender.task.accumulator

import io.holunda.camunda.taskpool.api.task.EngineTaskCommand

/**
 * Just passing the commands straight through.
 */
class DirectCommandAccumulator : EngineTaskCommandAccumulator {
  override fun invoke(taskCommands: List<EngineTaskCommand>) = taskCommands
}
