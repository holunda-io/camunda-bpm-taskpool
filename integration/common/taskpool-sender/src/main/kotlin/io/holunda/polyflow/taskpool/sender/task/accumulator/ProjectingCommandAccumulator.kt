package io.holunda.polyflow.taskpool.sender.task.accumulator

import com.fasterxml.jackson.databind.ObjectMapper
import io.holunda.camunda.taskpool.api.business.WithCorrelations
import io.holunda.camunda.taskpool.api.task.*
import io.holunda.camunda.taskpool.api.task.CamundaTaskEventType.Companion.CANDIDATE_GROUP_DELETE
import io.holunda.camunda.taskpool.api.task.CamundaTaskEventType.Companion.CANDIDATE_USER_DELETE
import io.holunda.camunda.variable.serializer.serialize
import kotlin.reflect.KClass


/**
 * Sorts commands by their order id and project attribute to one command
 * @param objectMapper optional object mapper used for serialization.
 * @param serializePayload
 */
class ProjectingCommandAccumulator(
  val objectMapper: ObjectMapper,
  val serializePayload: Boolean,
  simpleIntentDetectionBehaviour: Boolean
) : EngineTaskCommandAccumulator {

  private val engineTaskCommandIntentDetector = SimpleEngineTaskCommandIntentDetector(simpleIntentDetectionBehaviour)

  override fun invoke(taskCommands: List<EngineTaskCommand>): List<EngineTaskCommand> =
    // only if there are at least two commands, there is something to accumulate at all
    if (taskCommands.size > 1) {
      engineTaskCommandIntentDetector.detectIntents(taskCommands)
        .map { intent ->
          // after the sort, the first command is the actual intend and the remaining are carrying additional details.
          val command = intent.first()
          val details = intent.drop(1)
          projectCommandProperties(command, details)
        }.filter { taskCommand ->
          when (taskCommand) {
            // make sure the update historic task command is never detected as a primary intent
            is UpdateAttributesHistoricTaskCommand -> false
            else -> true
          }
        }
    } else {
      // otherwise just return the empty or singleton list, since there is nothing to do.
      taskCommands
    }.map {
      // serialize the content of payload and convert it to a map of key/value in order to be able
      // to deserializes without knowing the concrete classes
      serializePayloadIfNeeded(it)
    }

  /**
   * Take the original command and updates the properties based on the properties of details.
   */
  @Suppress("UNCHECKED_CAST")
  private fun <T : EngineTaskCommand> projectCommandProperties(command: T, details: List<T>): T {
    val mapper = jacksonMapper(objectMapper = objectMapper)
    return projectProperties(
      original = command,
      details = details,
      /**
       * Configuration to change default behavior of replacing the property.
       * For delta-commands (add/delete candidate groups/users), the lists has to be adjusted
       */
      propertyOperationConfig = mutableMapOf<KClass<out Any>, PropertyOperation>().apply {
        put(DeleteCandidateUsersCommand::class) { map, key, value ->
          if (key == DeleteCandidateUsersCommand::candidateUsers.name) {
            if (map.isTaskCommandEventType(CANDIDATE_USER_DELETE)) {
              // if the command is to delete candidate user, add the group to the list of deleted user
              map[key] = (map[key] as Collection<String>).plus(value as Collection<String>)
            } else {
              // a delete command should remove one element from candidate user
              map[key] = (map[key] as Collection<String>).minus(value as Collection<String>)
            }
          }
        }
        put(DeleteCandidateGroupsCommand::class) { map, key, value ->
          if (key == DeleteCandidateGroupsCommand::candidateGroups.name) {
            if (map.isTaskCommandEventType(CANDIDATE_GROUP_DELETE)) {
              // if the command is to delete candidate group, add the group to the list of deleted groups
              map[key] = (map[key] as Collection<String>).plus(value as Collection<String>)
            } else {
              // a delete command should remove one element from candidate group
              map[key] = (map[key] as Collection<String>).minus(value as Collection<String>)
            }
          }
        }
        // add command should add one element to candidate user
        put(AddCandidateUsersCommand::class) { map, key, value ->
          if (key == AddCandidateUsersCommand::candidateUsers.name) {
            map[key] = (map[key] as Collection<String>).plus(value as Collection<String>)
          }
        }
        // add command should add one element to candidate group
        put(AddCandidateGroupsCommand::class) { map, key, value ->
          if (key == AddCandidateGroupsCommand::candidateGroups.name) {
            map[key] = (map[key] as Collection<String>).plus(value as Collection<String>)
          }
        }
      },
      defaultPropertyOperation = { map, key, value ->
        if (key == TaskIdentity::sourceReference.name) {
          map[key] = value?.let { mapper.invoke(it) }
        } else {
          map[key] = value
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
        WithPayload::businessKey.name,
        // form key should not be changed - don't touch it
        WithFormKey::formKey.name
      ),
      projectionErrorDetector = EngineTaskCommandProjectionErrorDetector,
      mapper = mapper,
      unmapper = jacksonUnmapper(clazz = command::class.java, objectMapper = objectMapper)
    )
  }

  /*
   * Handle payload and serializes it using provided object mapper (e.g. to JSON)
   */
  @Suppress("UNCHECKED_CAST")
  private fun <T : EngineTaskCommand> serializePayloadIfNeeded(command: T): T =
    // FIXME: is there a way in Kotlin to avoid code duplication and make this check not type specific (but e.g. based on common interfaces)?
    if (serializePayload) {
      when (command) {
        is CreateTaskCommand -> command.copy(payload = serialize(payload = command.payload, mapper = objectMapper)) as T
        is UpdateAttributeTaskCommand -> command.copy(payload = serialize(payload = command.payload, mapper = objectMapper)) as T
        else -> command
      }
    } else {
      command
    }

  private fun Map<String, Any?>.isTaskCommandEventType(eventType: String): Boolean =
    requireNotNull(this[CamundaTaskEventType::eventName.name]) { "Property ${CamundaTaskEventType::eventName.name} must be set on command, but it was null." } == eventType
}

