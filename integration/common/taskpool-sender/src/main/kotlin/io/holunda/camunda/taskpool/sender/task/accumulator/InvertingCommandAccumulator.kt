package io.holunda.camunda.taskpool.sender.task.accumulator

import io.holunda.camunda.taskpool.api.task.EngineTaskCommand

/**
 * invert the order of commands, because Camunda sends them in reversed order.
 */
class InvertingCommandAccumulator : EngineTaskCommandAccumulator {
  override fun invoke(taskCommands: List<EngineTaskCommand>) = taskCommands.reversed()
}
