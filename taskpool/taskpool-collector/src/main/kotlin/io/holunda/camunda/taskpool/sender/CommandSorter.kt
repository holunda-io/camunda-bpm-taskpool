package io.holunda.camunda.taskpool.sender

import io.holunda.camunda.taskpool.api.task.EngineTaskCommand
import io.holunda.camunda.taskpool.api.task.WithTaskId

/**
 * Provides an ordering for commands. Actually, only EngineTaskCommand instances are ordered based on their order property.
 */
class CommandSorter : Comparator<WithTaskId> {

  override fun compare(command: WithTaskId, otherCommand: WithTaskId): Int {
    if (command is EngineTaskCommand && otherCommand is EngineTaskCommand) {
      // EngineTaskCommand with lower order value comes first
      return Integer.compare(command.order, otherCommand.order)
    }
    // Actually we expect only EngineTaskCommands here...
    // For the sake of consistency we provide an ordering here for other commands, too.
    // TODO: should we rather throw an excpetion instead? Or can we rule out the occurence of commands other than EngineTaskCommand anyway?
    if (command is EngineTaskCommand || otherCommand is EngineTaskCommand) {
      return if (command is EngineTaskCommand) -1 else 1
    }
    return 0
  }

}
