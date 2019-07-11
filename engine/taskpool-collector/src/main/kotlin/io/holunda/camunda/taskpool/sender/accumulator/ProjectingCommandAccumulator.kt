package io.holunda.camunda.taskpool.sender.accumulator

import io.holunda.camunda.taskpool.api.business.WithCorrelations
import io.holunda.camunda.taskpool.api.task.*
import io.holunda.camunda.variable.serializer.serialize
import kotlin.reflect.KClass


/**
 * sorts commands by their order id and project attribute to one command
 */
class ProjectingCommandAccumulator : CommandAccumulator {

  private val sorter: CommandAccumulator = SortingCommandAccumulator()

  override fun invoke(taskCommands: List<EngineTaskCommand>): List<EngineTaskCommand> =
    if (taskCommands.size > 1) {
      // only if there are at least two commands, there is something to accumulate at all
      val sorted = sorter.invoke(taskCommands)
      listOf(collectCommandProperties(sorted.first(), sorted.subList(1, sorted.size)))
    } else {
      // otherwise just return the empty or singleton list
      taskCommands
    }.map (::handlePayloadAndCorrelations)


  @Suppress("UNCHECKED_CAST")
  private fun <T : WithTaskId> collectCommandProperties(command: T, details: List<WithTaskId>): T = projectProperties(
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
        }
      }
      // a delete command should remove one element from candidate group
      put(DeleteCandidateGroupsCommand::class) { map, key, value ->
        if (key == DeleteCandidateGroupsCommand::candidateGroups.name) {
          (map[DeleteCandidateGroupsCommand::candidateGroups.name] as MutableCollection<String>).removeAll(value as Collection<String>)
        }
      }
      // add command should add one element to candidate user
      put(AddCandidateUsersCommand::class) { map, key, value ->
        if (key == AddCandidateUsersCommand::candidateUsers.name) {
          (map[AddCandidateUsersCommand::candidateUsers.name] as MutableCollection<String>).addAll(value as Collection<String>)
        }
      }
      // add command should add one element to candidate group
      put(AddCandidateGroupsCommand::class) { map, key, value ->
        if (key == AddCandidateGroupsCommand::candidateGroups.name) {
          (map[AddCandidateGroupsCommand::candidateGroups.name] as MutableCollection<String>).addAll(value as Collection<String>)
        }
      }
    },
    ignoredProperties = listOf(
      WithTaskId::id.name,
      CamundaTaskEvent::eventName.name,
      EngineTaskCommand::order.name,
      // handled separately
      WithPayload::payload.name,
      WithCorrelations::correlations.name
    )
  )

  /**
   * Handle payload and correlations and serialize using provided object mapper (e.g. to JSON)
   */
  @Suppress("UNCHECKED_CAST")
  fun <T : WithTaskId> handlePayloadAndCorrelations(command: T): T =
    // handle payload and correlations
    if (command is CreateTaskCommand)
      command.copy(payload = serialize(command.payload)) as T
    else
      command
}
