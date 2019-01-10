package io.holunda.camunda.taskpool.sender

import io.holunda.camunda.taskpool.api.task.*
import kotlin.reflect.KClass


typealias CommandAccumulator = (List<EngineTaskCommand>) -> List<EngineTaskCommand>

/**
 * Just passing the commands straight through.
 */
class DirectCommandAccumulator : CommandAccumulator {
  override fun invoke(taskCommands: List<EngineTaskCommand>) = taskCommands
}

/**
 * invert the order of commands, because Camunda sends them in reversed order.
 */
class InvertingCommandAccumulator : CommandAccumulator {
  override fun invoke(taskCommands: List<EngineTaskCommand>) = taskCommands.reversed()
}

/**
 * sorts commands by their order id
 */
class SortingCommandAccumulator : CommandAccumulator {
  override fun invoke(taskCommands: List<EngineTaskCommand>) = taskCommands.sortedWith(CommandSorter())
}

/**
 * sorts commands by their order id and project attribute to one command
 */
class ProjectingCommandAccumulator : CommandAccumulator {

  private val sorter: CommandAccumulator = SortingCommandAccumulator()

  override fun invoke(taskCommands: List<EngineTaskCommand>): List<EngineTaskCommand> {
    if (taskCommands.size > 1) {
      // only if there are at least two commands, there is something to accumulate at all
      val sorted = sorter.invoke(taskCommands)
      val command = sorted.first()
      return listOf(collectCommandProperties(command, sorted.subList(1, sorted.size)))
    }
    // otherwise just return the empty or singleton list
    return taskCommands
  }
}

class MergingCommandAccumulator : CommandAccumulator {

  private val sorter: CommandAccumulator = SortingCommandAccumulator()

  override fun invoke(taskCommands: List<EngineTaskCommand>): List<EngineTaskCommand> {
    val sorted = sorter.invoke(taskCommands)
    val firstCommand = sorted.first()
    val mergedCommands = mutableListOf(firstCommand)

    taskCommands.subList(1, taskCommands.size).forEach {
      // The first given command was taken as the basis for merging and added as initial command to the list of merged commands.
      // For each other given command, merge it with the last of the already merged commands.
      // The result is a list, either a singleton list with a new merged command, or two of them if the commands could not be merged.
      // That list is appended to the list of already merged command, replacing the last of the already merged commands.
      val currentCommand = mergedCommands.removeAt(mergedCommands.size - 1)
      val mergeResult = mergeCommands(currentCommand, it)
      mergedCommands.addAll(mergeResult)
    }
    return mergedCommands
  }

  private fun mergeCommands(command: EngineTaskCommand, otherCommand: EngineTaskCommand): List<EngineTaskCommand> =
    when (command) {
      is CreateTaskCommand -> listOf(collectCommandProperties(command, listOf(otherCommand)))
      is AssignTaskCommand -> {
        when (otherCommand) {
          is UpdateAssignmentTaskCommand -> listOf(collectCommandProperties(command, listOf(otherCommand)))
          is UpdateAttributeTaskCommand -> listOf(mergeToUpdateTaskCommand(command, otherCommand))
          else -> listOf(command, otherCommand)
        }
      }
      else -> listOf(command, otherCommand)
    }

  private fun mergeToUpdateTaskCommand(assignTaskCommand: AssignTaskCommand, updateAttributeTaskCommand: UpdateAttributeTaskCommand): UpdateTaskCommand =
    UpdateTaskCommand(
      id = assignTaskCommand.id,
      name = updateAttributeTaskCommand.name,
      description = updateAttributeTaskCommand.description,
      priority = updateAttributeTaskCommand.priority,
      owner = updateAttributeTaskCommand.owner,
      assignee = assignTaskCommand.assignee,
      dueDate = updateAttributeTaskCommand.dueDate,
      followUpDate = updateAttributeTaskCommand.followUpDate,
      candidateUsers = assignTaskCommand.candidateUsers,
      candidateGroups = assignTaskCommand.candidateGroups)
}


fun <T : WithTaskId> collectCommandProperties(command: T, details: List<WithTaskId>): T = projectProperties(
  original = command,
  details = details,
  /**
   * Configuration to change default behavior of replacing the property.
   * For delta-commands (add/delete candidate groups/users), the lists has to be adjusted
   */
  propertyOperationConfig = mutableMapOf<KClass<out Any>, PropertyOperation>().apply {
    // a delete command should remove one element from candidate user
    put(DeleteCandidateUsersCommand::class) { map, key, value ->
      if (key == DeleteCandidateUsersCommand::candidateUsers.name) {
        (map[DeleteCandidateUsersCommand::candidateUsers.name] as MutableCollection<String>).removeAll(value as Collection<String>)
      } else {
        map[key] = value
      }
    }
    // a delete command should remove one element from candidate group
    put(DeleteCandidateGroupsCommand::class) { map, key, value ->
      if (key == DeleteCandidateGroupsCommand::candidateGroups.name) {
        (map[DeleteCandidateGroupsCommand::candidateGroups.name] as MutableCollection<String>).removeAll(value as Collection<String>)
      } else {
        map[key] = value
      }
    }
    // add command should add one element to candidate user
    put(AddCandidateUsersCommand::class) { map, key, value ->
      if (key == AddCandidateUsersCommand::candidateUsers.name) {
        (map[AddCandidateUsersCommand::candidateUsers.name] as MutableCollection<String>).addAll(value as Collection<String>)
      } else {
        map[key] = value
      }
    }
    // add command should add one element to candidate group
    put(AddCandidateGroupsCommand::class) { map, key, value ->
      if (key == AddCandidateGroupsCommand::candidateGroups.name) {
        (map[AddCandidateGroupsCommand::candidateGroups.name] as MutableCollection<String>).addAll(value as Collection<String>)
      } else {
        map[key] = value
      }
    }
  }
)
