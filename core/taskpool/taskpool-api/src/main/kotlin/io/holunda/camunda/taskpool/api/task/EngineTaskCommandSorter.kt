package io.holunda.camunda.taskpool.api.task

/**
 * Create can carry almost everything of changes.
 */
const val ORDER_TASK_CREATION = -3

/**
 * Complete can carry assignee.
 */
const val ORDER_TASK_COMPLETION = -2
const val ORDER_TASK_DELETION = -1
const val ORDER_TASK_ASSIGNMENT = 0
const val ORDER_TASK_CANDIDATES_UPDATE = 1
const val ORDER_TASK_ATTRIBUTE_UPDATE = 2
const val ORDER_TASK_BATCH = 100

/**
 * Special command that is never an intent, but is used for enrichment of other commands.
 */
const val ORDER_TASK_HISTORIC_ATTRIBUTE_UPDATE = 10


/**
 * Provides an ordering for EngineTaskCommand based on their order property.
 * This is used to determine the intent of the command batch.
 */
class EngineTaskCommandSorter : Comparator<EngineTaskCommand> {

  override fun compare(command: EngineTaskCommand, otherCommand: EngineTaskCommand): Int {

    // EngineTaskCommand with lower order value comes first
    return command.order.compareTo(otherCommand.order)
  }
}
