package io.holunda.camunda.taskpool.sender

import io.holunda.camunda.taskpool.api.task.*
import kotlin.reflect.KClass


typealias CommandAccumulator = (List<WithTaskId>) -> List<WithTaskId>

/**
 * Just passing the commands straight through.
 */
class DirectCommandAccumulator : CommandAccumulator {
  override fun invoke(taskCommands: List<WithTaskId>) = taskCommands
}

/**
 * invert the order of commands, because Camunda sends them in reversed order.
 */
class InvertingCommandAccumulator : CommandAccumulator {
  override fun invoke(taskCommands: List<WithTaskId>) = taskCommands.reversed()
}

/**
 * invert the order of commands, because Camunda sends them in reversed order.
 */
class SortingCommandAccumulator : CommandAccumulator {
  override fun invoke(taskCommands: List<WithTaskId>) = taskCommands.sortedWith(CommandSorter())
}

/**
 * invert the order of commands and project attribute to one command
 */
class ProjectingCommandAccumulator : CommandAccumulator {

  private val sorter: CommandAccumulator = SortingCommandAccumulator()

  override fun invoke(taskCommands: List<WithTaskId>): List<WithTaskId> {
    val sorted = sorter.invoke(taskCommands)
    var command = sorted.first()
    return listOf(collectCommandProperties(command, sorted.subList(1, sorted.size - 1)))
  }
}

fun <T : WithTaskId> collectCommandProperties(command: T, details: List<WithTaskId>): T = projectProperties(
  original = command,
  details = details,
  /**
   * Configuration to change default behavior of replacing the property.
   * For delta-commands (add/delete candidate groups/users), the lists has to be adjusted
   */
  opContainerConfig = mutableMapOf<KClass<out Any>, ContainerOperation>().apply {
    // a delete command should remove one element from candidate user
    // FIXME: rename command property to candidateUsers and make it to a list
    put(DeleteCandidateUserCommand::class) { map, key, value ->
      when (val original = map[key]) {
        is MutableCollection<*> -> (value as MutableCollection<String>).forEach { (original as MutableCollection<String>).remove(it) }
        else -> map[key] = value
      }
    }
    // a delete command should remove one element from candidate group
    // FIXME: rename command property to candidateGroups and make it to a list
    put(DeleteCandidateGroupCommand::class) { map, key, value ->
      when (val original = map[key]) {
        is MutableCollection<*> -> (value as MutableCollection<String>).forEach { (original as MutableCollection<String>).remove(it) }
        else -> map[key] = value
      }
    }
    // a delete command should remove one element from candidate user
    // FIXME: rename command property to candidateUsers and make it to a list
    put(AddCandidateUserCommand::class) { map, key, value ->
      when (val original = map[key]) {
        is MutableCollection<*> -> (value as MutableCollection<String>).forEach { (original as MutableCollection<String>).add(it) }
        else -> map[key] = value
      }
    }
    // a delete command should remove one element from candidate group
    // FIXME: rename command property to candidateGroups and make it to a list
    put(AddCandidateGroupCommand::class) { map, key, value ->
      when (val original = map[key]) {
        is MutableCollection<*> -> (value as MutableCollection<String>).forEach { (original as MutableCollection<String>).add(it) }
        else -> map[key] = value
      }
    }
  }
)
