package io.holunda.camunda.taskpool.sender.task.accumulator

import com.fasterxml.jackson.databind.ObjectMapper
import io.holunda.camunda.taskpool.api.business.WithCorrelations
import io.holunda.camunda.taskpool.api.task.*
import io.holunda.camunda.variable.serializer.serialize
import kotlin.reflect.KClass


/**
 * Sorts commands by their order id and project attribute to one command
 * @param objectMapper optional object mapper used for serialization.
 */
class ProjectingCommandAccumulator(
  val objectMapper: ObjectMapper
) : EngineTaskCommandAccumulator {

  private val sorter: EngineTaskCommandAccumulator = SortingCommandAccumulator()

  override fun invoke(taskCommands: List<EngineTaskCommand>): List<EngineTaskCommand> =
    if (taskCommands.size > 1) {
      // only if there are at least two commands, there is something to accumulate at all
      val sorted = sorter.invoke(taskCommands)
      // after the sort, the first command is the actual intend and the remaining are carrying additional details.
      listOf(projectCommandProperties(sorted.first(), sorted.subList(1, sorted.size)))
    } else {
      // otherwise just return the empty or singleton list
      taskCommands
    }.map {
      // serialize the content of payload and convert it to a map of key/value in order to be able
      // to deserializes without knowing the concrete classes
      serializePayload(it)
    }

  /**
   * Take the original command and updates the properties based on the properties of details.
   */
  @Suppress("UNCHECKED_CAST")
  private fun <T : EngineTaskCommand> projectCommandProperties(command: T, details: List<T>): T = projectProperties(
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
          val candidateUsers = (map[key] as Collection<String>).minus(value as Collection<String>)
          map[key] = candidateUsers
        }
      }
      // a delete command should remove one element from candidate group
      put(DeleteCandidateGroupsCommand::class) { map, key, value ->
        if (key == DeleteCandidateGroupsCommand::candidateGroups.name) {
          val candidateGroups = (map[key] as Collection<String>).minus(value as Collection<String>)
          map[key] = candidateGroups
        }
      }
      // add command should add one element to candidate user
      put(AddCandidateUsersCommand::class) { map, key, value ->
        if (key == AddCandidateUsersCommand::candidateUsers.name) {
          val candidateUsers = (map[key] as Collection<String>).plus(value as Collection<String>)
          map[key] = candidateUsers
        }
      }
      // add command should add one element to candidate group
      put(AddCandidateGroupsCommand::class) { map, key, value ->
        if (key == AddCandidateGroupsCommand::candidateGroups.name) {
          val candidateGroups = (map[key] as Collection<String>).plus(value as Collection<String>)
          map[key] = candidateGroups
        }
      }
    },
    ignoredProperties = listOf(
      WithTaskId::id.name,
      CamundaTaskEventType::eventName.name,
      EngineTaskCommand::order.name,
      // no reason to overwrite correlation information or payload ever
      // the initial command (create, update) already contains the entire information.
      WithCorrelations::correlations.name,
      WithPayload::payload.name,
      // there is no reason to overwrite a business key so far
      // its initial value is read and send during task creation
      WithPayload::businessKey.name
    ),
    projectionErrorDetector = EngineTaskCommandProjectionErrorDetector,
    mapper = jacksonMapper(objectMapper = objectMapper),
    unmapper = jacksonUnmapper(clazz = command::class.java, objectMapper = objectMapper)
  )

  /**
   * Handle payload and serializes it using provided object mapper (e.g. to JSON)
   */
  @Suppress("UNCHECKED_CAST")
  fun <T : EngineTaskCommand> serializePayload(command: T): T =
    // FIXME: is there a way in Kotlin to avoid code duplication and make this check not type specific (but e.g. based on common interfaces)?
    when (command) {
      is CreateTaskCommand -> command.copy(payload = serialize(payload = command.payload, mapper = objectMapper)) as T
      is UpdateAttributeTaskCommand -> command.copy(payload = serialize(payload = command.payload, mapper = objectMapper)) as T
      else -> command
    }
}

/**
 * Due to Camunda event handling implementation eventing might be slightly strange.
 * Ignore error reporting if to any original the detail is AddCandidateUsersCommand or UpdateAttributeTaskCommand, since both those commands
 * should be primary intent (original) and not detail.
 */
object EngineTaskCommandProjectionErrorDetector : ProjectionErrorDetector {

  override fun shouldReportError(original: Any, detail: Any): Boolean {
    return when (detail) {
      is AddCandidateUsersCommand, is UpdateAttributeTaskCommand -> false
      else -> true
    }
  }
}
