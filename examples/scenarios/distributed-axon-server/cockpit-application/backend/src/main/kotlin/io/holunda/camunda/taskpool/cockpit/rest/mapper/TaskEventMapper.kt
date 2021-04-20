package io.holunda.camunda.taskpool.cockpit.rest.mapper

import io.holunda.camunda.taskpool.api.task.*
import io.holunda.camunda.taskpool.cockpit.rest.model.TaskEventDto
import io.holunda.camunda.taskpool.cockpit.service.TaskEventWithMetaData
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Mappings
import org.mapstruct.ReportingPolicy
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*
import javax.validation.Valid

/**
 * DTO mapper.
 */
@Mapper(
  componentModel = "spring",
  unmappedTargetPolicy = ReportingPolicy.ERROR
)
abstract class TaskEventMapper {

  /**
   * Maps to DTO.
   */
  fun dto(withMetadata: TaskEventWithMetaData) =
    when (withMetadata.event) {

      // Interaction
      is TaskToBeCompletedEvent -> dto(withMetadata.event, withMetadata.instant)
      is TaskClaimedEvent -> dto(withMetadata.event, withMetadata.instant)
      is TaskUnclaimedEvent -> dto(withMetadata.event, withMetadata.instant)
      is TaskDeferredEvent -> dto(withMetadata.event, withMetadata.instant)
      is TaskUndeferredEvent -> dto(withMetadata.event, withMetadata.instant)

      // Engine
      is TaskCompletedEngineEvent -> dto(withMetadata.event, withMetadata.instant)
      is TaskDeletedEngineEvent -> dto(withMetadata.event, withMetadata.instant)
      is TaskCreatedEngineEvent -> dto(withMetadata.event, withMetadata.instant)
      is TaskAssignedEngineEvent -> dto(withMetadata.event, withMetadata.instant)

      is TaskAttributeUpdatedEngineEvent -> dto(withMetadata.event, withMetadata.instant)
      is TaskCandidateGroupChanged -> dto(withMetadata.event, withMetadata.instant)
      is TaskCandidateUserChanged -> dto(withMetadata.event, withMetadata.instant)

      else -> throw IllegalArgumentException("Unexpected type of $withMetadata: ${withMetadata.javaClass}")
    }


  /**
   * Maps to DTO.
   */
  @Mappings(
    Mapping(target = "task.id", source = "task.id"),
    Mapping(target = "task.formKey", source = "task.formKey"),
    Mapping(target = "task.taskDefinitionKey", source = "task.taskDefinitionKey"),
    Mapping(target = "task.processName", source = "task.sourceReference.name"),
    Mapping(target = "task.processDefinitionKey", source = "task.sourceReference.definitionKey"),
    Mapping(target = "task.applicationName", source = "task.sourceReference.applicationName"),
    Mapping(target = "task.tenantId", source = "task.sourceReference.tenantId"),

    Mapping(target = "task.name", source = "task.name"),
    Mapping(target = "task.description", source = "task.description"),
    Mapping(target = "task.candidateUsers", source = "task.candidateUsers"),
    Mapping(target = "task.candidateGroups", source = "task.candidateGroups"),
    Mapping(target = "task.assignee", source = "task.assignee"),
    Mapping(target = "task.createTime", source = "task.createTime"),
    Mapping(target = "task.dueDate", source = "task.dueDate"),
    Mapping(target = "task.businessKey", source = "task.businessKey"),
    Mapping(target = "task.priority", source = "task.priority"),
    Mapping(target = "task.payload", source = "task.payload"),
    Mapping(target = "task.followUpDate", source = "task.followUpDate"),

    Mapping(target = "id", source = "task.id"),
    Mapping(target = "eventType", source = "task.eventType"),
    Mapping(target = "created", source = "instant")
  )
  abstract fun dto(task: TaskCreatedEngineEvent, instant: Instant?): TaskEventDto

  /**
   * Maps to DTO.
   */
  @Mappings(
    Mapping(target = "task.id", source = "task.id"),
    Mapping(target = "task.formKey", source = "task.formKey"),
    Mapping(target = "task.taskDefinitionKey", source = "task.taskDefinitionKey"),
    Mapping(target = "task.processName", source = "task.sourceReference.name"),
    Mapping(target = "task.processDefinitionKey", source = "task.sourceReference.definitionKey"),
    Mapping(target = "task.applicationName", source = "task.sourceReference.applicationName"),
    Mapping(target = "task.tenantId", source = "task.sourceReference.tenantId"),

    Mapping(target = "task.name", source = "task.name"),
    Mapping(target = "task.description", source = "task.description"),
    Mapping(target = "task.candidateUsers", source = "task.candidateUsers"),
    Mapping(target = "task.candidateGroups", source = "task.candidateGroups"),
    Mapping(target = "task.assignee", source = "task.assignee"),
    Mapping(target = "task.createTime", source = "task.createTime"),
    Mapping(target = "task.dueDate", source = "task.dueDate"),
    Mapping(target = "task.businessKey", source = "task.businessKey"),
    Mapping(target = "task.priority", source = "task.priority"),
    Mapping(target = "task.payload", source = "task.payload"),
    Mapping(target = "task.followUpDate", source = "task.followUpDate"),

    Mapping(target = "id", source = "task.id"),
    Mapping(target = "eventType", source = "task.eventType"),
    Mapping(target = "created", source = "instant")
  )
  abstract fun dto(task: TaskDeletedEngineEvent, instant: Instant?): TaskEventDto

  /**
   * Create a DTO.
   */
  @Mappings(
    Mapping(target = "task.id", source = "task.id"),
    Mapping(target = "task.formKey", source = "task.formKey"),
    Mapping(target = "task.taskDefinitionKey", source = "task.taskDefinitionKey"),
    Mapping(target = "task.processName", source = "task.sourceReference.name"),
    Mapping(target = "task.processDefinitionKey", source = "task.sourceReference.definitionKey"),
    Mapping(target = "task.applicationName", source = "task.sourceReference.applicationName"),
    Mapping(target = "task.tenantId", source = "task.sourceReference.tenantId"),

    Mapping(target = "task.name", source = "task.name"),
    Mapping(target = "task.description", source = "task.description"),
    Mapping(target = "task.candidateUsers", source = "task.candidateUsers"),
    Mapping(target = "task.candidateGroups", source = "task.candidateGroups"),
    Mapping(target = "task.assignee", source = "task.assignee"),
    Mapping(target = "task.createTime", source = "task.createTime"),
    Mapping(target = "task.dueDate", source = "task.dueDate"),
    Mapping(target = "task.businessKey", source = "task.businessKey"),
    Mapping(target = "task.priority", source = "task.priority"),
    Mapping(target = "task.payload", source = "task.payload"),
    Mapping(target = "task.followUpDate", source = "task.followUpDate"),

    Mapping(target = "id", source = "task.id"),
    Mapping(target = "eventType", source = "task.eventType"),
    Mapping(target = "created", source = "instant")
  )
  abstract fun dto(task: TaskAssignedEngineEvent, instant: Instant?): TaskEventDto

  /**
   * Create a DTO.
   */
  @Mappings(
    Mapping(target = "task.id", source = "task.id"),
    Mapping(target = "task.formKey", ignore = true),
    Mapping(target = "task.taskDefinitionKey", source = "task.taskDefinitionKey"),
    Mapping(target = "task.processName", source = "task.sourceReference.name"),
    Mapping(target = "task.processDefinitionKey", source = "task.sourceReference.definitionKey"),
    Mapping(target = "task.applicationName", source = "task.sourceReference.applicationName"),
    Mapping(target = "task.tenantId", source = "task.sourceReference.tenantId"),

    Mapping(target = "task.name", source = "task.name"),
    Mapping(target = "task.description", source = "task.description"),
    Mapping(target = "task.candidateUsers", ignore = true),
    Mapping(target = "task.candidateGroups", ignore = true),
    Mapping(target = "task.assignee", ignore = true),
    Mapping(target = "task.createTime", ignore = true),
    Mapping(target = "task.dueDate", source = "task.dueDate"),
    Mapping(target = "task.businessKey", ignore = true),
    Mapping(target = "task.priority", source = "task.priority"),
    Mapping(target = "task.payload", ignore = true),
    Mapping(target = "task.followUpDate", source = "task.followUpDate"),

    Mapping(target = "id", source = "task.id"),
    Mapping(target = "eventType", source = "task.eventType"),
    Mapping(target = "created", source = "instant")
  )
  abstract fun dto(task: TaskAttributeUpdatedEngineEvent, instant: Instant?): TaskEventDto

  /**
   * Create a DTO.
   */
  @Mappings(
    Mapping(target = "task.id", source = "task.id"),
    Mapping(target = "task.formKey", source = "task.formKey"),
    Mapping(target = "task.taskDefinitionKey", source = "task.taskDefinitionKey"),
    Mapping(target = "task.processName", source = "task.sourceReference.name"),
    Mapping(target = "task.processDefinitionKey", source = "task.sourceReference.definitionKey"),
    Mapping(target = "task.applicationName", source = "task.sourceReference.applicationName"),
    Mapping(target = "task.tenantId", source = "task.sourceReference.tenantId"),

    Mapping(target = "task.name", source = "task.name"),
    Mapping(target = "task.description", source = "task.description"),
    Mapping(target = "task.candidateUsers", source = "task.candidateUsers"),
    Mapping(target = "task.candidateGroups", source = "task.candidateGroups"),
    Mapping(target = "task.assignee", source = "task.assignee"),
    Mapping(target = "task.createTime", source = "task.createTime"),
    Mapping(target = "task.dueDate", source = "task.dueDate"),
    Mapping(target = "task.businessKey", source = "task.businessKey"),
    Mapping(target = "task.priority", source = "task.priority"),
    Mapping(target = "task.payload", source = "task.payload"),
    Mapping(target = "task.followUpDate", source = "task.followUpDate"),

    Mapping(target = "id", source = "task.id"),
    Mapping(target = "eventType", source = "task.eventType"),
    Mapping(target = "created", source = "instant")
  )
  abstract fun dto(task: TaskCompletedEngineEvent, instant: Instant?): TaskEventDto

  /**
   * Maps to DTO.
   */
  @Mappings(
    Mapping(target = "task.id", source = "task.id"),
    Mapping(target = "task.formKey", ignore = true),
    Mapping(target = "task.taskDefinitionKey", source = "task.taskDefinitionKey"),
    Mapping(target = "task.processName", source = "task.sourceReference.name"),
    Mapping(target = "task.processDefinitionKey", source = "task.sourceReference.definitionKey"),
    Mapping(target = "task.applicationName", source = "task.sourceReference.applicationName"),
    Mapping(target = "task.tenantId", source = "task.sourceReference.tenantId"),

    Mapping(target = "task.name", ignore = true),
    Mapping(target = "task.description", ignore = true),
    Mapping(target = "task.candidateUsers", ignore = true),
    Mapping(target = "task.candidateGroups", ignore = true),
    Mapping(target = "task.assignee", ignore = true),
    Mapping(target = "task.createTime", ignore = true),
    Mapping(target = "task.dueDate", ignore = true),
    Mapping(target = "task.businessKey", ignore = true),
    Mapping(target = "task.priority", ignore = true),
    Mapping(target = "task.payload", ignore = true),
    Mapping(target = "task.followUpDate", ignore = true),

    Mapping(target = "id", source = "task.id"),
    Mapping(target = "eventType", source = "task.eventType"),
    Mapping(target = "created", source = "instant")
  )
  abstract fun dto(task: TaskClaimedEvent, instant: Instant?): TaskEventDto

  /**
   * Maps to DTO.
   */
  @Mappings(
    Mapping(target = "task.id", source = "task.id"),
    Mapping(target = "task.formKey", ignore = true),
    Mapping(target = "task.taskDefinitionKey", source = "task.taskDefinitionKey"),
    Mapping(target = "task.processName", source = "task.sourceReference.name"),
    Mapping(target = "task.processDefinitionKey", source = "task.sourceReference.definitionKey"),
    Mapping(target = "task.applicationName", source = "task.sourceReference.applicationName"),
    Mapping(target = "task.tenantId", source = "task.sourceReference.tenantId"),

    Mapping(target = "task.name", ignore = true),
    Mapping(target = "task.description", ignore = true),
    Mapping(target = "task.candidateUsers", ignore = true),
    Mapping(target = "task.candidateGroups", ignore = true),
    Mapping(target = "task.assignee", ignore = true),
    Mapping(target = "task.createTime", ignore = true),
    Mapping(target = "task.dueDate", ignore = true),
    Mapping(target = "task.businessKey", ignore = true),
    Mapping(target = "task.priority", ignore = true),
    Mapping(target = "task.payload", ignore = true),
    Mapping(target = "task.followUpDate", ignore = true),

    Mapping(target = "id", source = "task.id"),
    Mapping(target = "eventType", source = "task.eventType"),
    Mapping(target = "created", source = "instant")
  )
  abstract fun dto(task: TaskUnclaimedEvent, instant: Instant?): TaskEventDto

  /**
   * Maps to DTO.
   */
  @Mappings(
    Mapping(target = "task.id", source = "task.id"),
    Mapping(target = "task.formKey", ignore = true),
    Mapping(target = "task.taskDefinitionKey", source = "task.taskDefinitionKey"),
    Mapping(target = "task.processName", source = "task.sourceReference.name"),
    Mapping(target = "task.processDefinitionKey", source = "task.sourceReference.definitionKey"),
    Mapping(target = "task.applicationName", source = "task.sourceReference.applicationName"),
    Mapping(target = "task.tenantId", source = "task.sourceReference.tenantId"),

    Mapping(target = "task.name", ignore = true),
    Mapping(target = "task.description", ignore = true),
    Mapping(target = "task.candidateUsers", ignore = true),
    Mapping(target = "task.candidateGroups", ignore = true),
    Mapping(target = "task.assignee", ignore = true),
    Mapping(target = "task.createTime", ignore = true),
    Mapping(target = "task.dueDate", ignore = true),
    Mapping(target = "task.businessKey", ignore = true),
    Mapping(target = "task.priority", ignore = true),
    Mapping(target = "task.payload", ignore = true),
    Mapping(target = "task.followUpDate", ignore = true),

    Mapping(target = "id", source = "task.id"),
    Mapping(target = "eventType", source = "task.eventType"),
    Mapping(target = "created", source = "instant")
  )
  abstract fun dto(task: TaskUndeferredEvent, instant: Instant?): TaskEventDto

  /**
   * Maps to DTO.
   */
  @Mappings(
    Mapping(target = "task.id", source = "task.id"),
    Mapping(target = "task.formKey", ignore = true),
    Mapping(target = "task.taskDefinitionKey", source = "task.taskDefinitionKey"),
    Mapping(target = "task.processName", source = "task.sourceReference.name"),
    Mapping(target = "task.processDefinitionKey", source = "task.sourceReference.definitionKey"),
    Mapping(target = "task.applicationName", source = "task.sourceReference.applicationName"),
    Mapping(target = "task.tenantId", source = "task.sourceReference.tenantId"),

    Mapping(target = "task.name", ignore = true),
    Mapping(target = "task.description", ignore = true),
    Mapping(target = "task.candidateUsers", ignore = true),
    Mapping(target = "task.candidateGroups", ignore = true),
    Mapping(target = "task.assignee", ignore = true),
    Mapping(target = "task.createTime", ignore = true),
    Mapping(target = "task.dueDate", ignore = true),
    Mapping(target = "task.businessKey", ignore = true),
    Mapping(target = "task.priority", ignore = true),
    Mapping(target = "task.payload", ignore = true),
    Mapping(target = "task.followUpDate", source = "task.followUpDate"),

    Mapping(target = "id", source = "task.id"),
    Mapping(target = "eventType", source = "task.eventType"),
    Mapping(target = "created", source = "instant")
  )
  abstract fun dto(task: TaskDeferredEvent, instant: Instant?): TaskEventDto

  /**
   * Maps to DTO.
   */
  @Mappings(
    Mapping(target = "task.id", source = "task.id"),
    Mapping(target = "task.taskDefinitionKey", source = "task.taskDefinitionKey"),
    Mapping(target = "task.formKey", ignore = true),
    Mapping(target = "task.processName", source = "task.sourceReference.name"),
    Mapping(target = "task.processDefinitionKey", source = "task.sourceReference.definitionKey"),
    Mapping(target = "task.applicationName", source = "task.sourceReference.applicationName"),
    Mapping(target = "task.tenantId", source = "task.sourceReference.tenantId"),

    Mapping(target = "task.name", ignore = true),
    Mapping(target = "task.description", ignore = true),
    Mapping(target = "task.candidateUsers", ignore = true),
    Mapping(target = "task.candidateGroups", ignore = true),
    Mapping(target = "task.assignee", ignore = true),
    Mapping(target = "task.createTime", ignore = true),
    Mapping(target = "task.dueDate", ignore = true),
    Mapping(target = "task.businessKey", ignore = true),
    Mapping(target = "task.priority", ignore = true),
    Mapping(target = "task.payload", source = "task.payload"),

    Mapping(target = "id", source = "task.id"),
    Mapping(target = "eventType", source = "task.eventType"),
    Mapping(target = "created", source = "instant")
  )
  abstract fun dto(task: TaskToBeCompletedEvent, instant: Instant?): TaskEventDto

  /**
   * Maps to DTO.
   */
  @Mappings(
    Mapping(target = "task.id", source = "task.id"),
    Mapping(target = "task.formKey", ignore = true),
    Mapping(target = "task.taskDefinitionKey", source = "task.taskDefinitionKey"),
    Mapping(target = "task.processName", source = "task.sourceReference.name"),
    Mapping(target = "task.processDefinitionKey", source = "task.sourceReference.definitionKey"),
    Mapping(target = "task.applicationName", source = "task.sourceReference.applicationName"),
    Mapping(target = "task.tenantId", source = "task.sourceReference.tenantId"),

    Mapping(target = "task.name", ignore = true),
    Mapping(target = "task.description", ignore = true),
    Mapping(target = "task.candidateUsers", ignore = true),
    Mapping(target = "task.candidateGroups", ignore = true),
    Mapping(target = "task.assignee", ignore = true),
    Mapping(target = "task.createTime", ignore = true),
    Mapping(target = "task.dueDate", ignore = true),
    Mapping(target = "task.businessKey", ignore = true),
    Mapping(target = "task.priority", ignore = true),
    Mapping(target = "task.payload", ignore = true),

    Mapping(target = "id", source = "task.id"),
    Mapping(target = "eventType", source = "task.eventType"),
    Mapping(target = "created", source = "instant")
  )
  abstract fun dto(task: TaskCandidateGroupChanged, instant: Instant?): TaskEventDto

  /**
   * Maps to DTO.
   */
  @Mappings(
    Mapping(target = "task.id", source = "task.id"),
    Mapping(target = "task.formKey", ignore = true),
    Mapping(target = "task.taskDefinitionKey", source = "task.taskDefinitionKey"),
    Mapping(target = "task.processName", source = "task.sourceReference.name"),
    Mapping(target = "task.processDefinitionKey", source = "task.sourceReference.definitionKey"),
    Mapping(target = "task.applicationName", source = "task.sourceReference.applicationName"),
    Mapping(target = "task.tenantId", source = "task.sourceReference.tenantId"),

    Mapping(target = "task.name", ignore = true),
    Mapping(target = "task.description", ignore = true),
    Mapping(target = "task.candidateUsers", ignore = true),
    Mapping(target = "task.candidateGroups", ignore = true),
    Mapping(target = "task.assignee", ignore = true),
    Mapping(target = "task.createTime", ignore = true),
    Mapping(target = "task.dueDate", ignore = true),
    Mapping(target = "task.businessKey", ignore = true),
    Mapping(target = "task.priority", ignore = true),
    Mapping(target = "task.payload", ignore = true),

    Mapping(target = "id", source = "task.id"),
    Mapping(target = "eventType", source = "task.eventType"),
    Mapping(target = "created", source = "instant")
  )
  abstract fun dto(task: TaskCandidateUserChanged, instant: Instant?): TaskEventDto

  /**
   * Date converter.
   */
  fun toOffsetDateTime(@Valid time: Date?): OffsetDateTime? {
    return if (time == null) null else toOffsetDateTime(time.toInstant())
  }

  /**
   * Date converter.
   */
  fun toOffsetDateTime(@Valid instant: Instant?): OffsetDateTime? {
    return if (instant == null) null else OffsetDateTime.ofInstant(instant, ZoneOffset.UTC)
  }
}
