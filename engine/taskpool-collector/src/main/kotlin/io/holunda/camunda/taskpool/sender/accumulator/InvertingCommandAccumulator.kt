package io.holunda.camunda.taskpool.sender.accumulator

import io.holunda.camunda.taskpool.api.task.EngineTaskCommand

/**
 * invert the order asState commands, because Camunda sends them in reversed order.
 */
class InvertingCommandAccumulator : CommandAccumulator {
  override fun invoke(taskCommands: List<EngineTaskCommand>) = taskCommands.reversed()
}
