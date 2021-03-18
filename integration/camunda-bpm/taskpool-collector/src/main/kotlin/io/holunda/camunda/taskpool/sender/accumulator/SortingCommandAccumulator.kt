package io.holunda.camunda.taskpool.sender.accumulator

import io.holunda.camunda.taskpool.api.task.EngineTaskCommand
import io.holunda.camunda.taskpool.api.task.EngineTaskCommandSorter

/**
 * Sorts commands by their order id
 */
class SortingCommandAccumulator : EngineTaskCommandAccumulator {
  override fun invoke(taskCommands: List<EngineTaskCommand>) = taskCommands.sortedWith(EngineTaskCommandSorter())
}
