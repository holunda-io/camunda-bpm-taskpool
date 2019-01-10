package io.holunda.camunda.taskpool.sender

import io.holunda.camunda.taskpool.api.task.EngineTaskCommand

/**
 * Provides an ordering for EngineTaskCommand based on their order property.
 */
class CommandSorter : Comparator<EngineTaskCommand> {

  override fun compare(command: EngineTaskCommand, otherCommand: EngineTaskCommand): Int {
    // EngineTaskCommand with lower order value comes first
    return Integer.compare(command.order, otherCommand.order)
  }

}
