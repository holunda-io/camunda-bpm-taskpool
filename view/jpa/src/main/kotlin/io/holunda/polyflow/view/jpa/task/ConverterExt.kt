package io.holunda.polyflow.view.jpa.task

import com.fasterxml.jackson.databind.ObjectMapper
import io.holunda.camunda.taskpool.api.task.TaskAttributeUpdatedEngineEvent
import io.holunda.camunda.taskpool.api.task.TaskCreatedEngineEvent
import io.holunda.camunda.variable.serializer.*
import io.holunda.polyflow.view.Task
import io.holunda.polyflow.view.jpa.auth.AuthorizationPrincipal
import io.holunda.polyflow.view.jpa.data.DataEntryId
import io.holunda.polyflow.view.jpa.data.asGroupnames
import io.holunda.polyflow.view.jpa.data.asUsernames
import io.holunda.polyflow.view.jpa.payload.PayloadAttribute
import io.holunda.polyflow.view.jpa.process.toSourceReference
import io.holunda.polyflow.view.jpa.process.toSourceReferenceEmbeddable
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables
import java.time.Instant


/**
 * Event to entity.
 */
fun TaskCreatedEngineEvent.toEntity(
  objectMapper: ObjectMapper,
  limit: Int,
  filters: List<Pair<JsonPathFilterFunction, FilterType>>
) = TaskEntity(
  taskId = this.id,
  taskDefinitionKey = this.taskDefinitionKey,
  name = this.name ?: "",
  priority = this.priority ?: 50,
  sourceReference = this.sourceReference.toSourceReferenceEmbeddable(),
  authorizedPrincipals = this.candidateUsers.map { AuthorizationPrincipal.user(it).toString() }
    .plus(this.candidateGroups.map { AuthorizationPrincipal.group(it).toString() })
    .toMutableSet(),
  correlations = this.correlations.map { entry -> DataEntryId(entryType = entry.key, entryId = "${entry.value}") }.toMutableSet(),
  payload = this.payload.toPayloadJson(objectMapper),
  payloadAttributes = this.payload.toJsonPathsWithValues(limit, filters).map { attr -> PayloadAttribute(attr) }.toMutableSet(),
  assignee = this.assignee,
  businessKey = this.businessKey,
  description = this.description,
  formKey = this.formKey,
  createdDate = this.createTime?.toInstant() ?: Instant.now(),
  followUpDate = this.followUpDate?.toInstant(),
  dueDate = this.dueDate?.toInstant(),
  owner = this.owner,
)

/**
 * Applies a TaskAttributeUpdatedEngineEvent update to an existing TaskEntity
 */
fun TaskEntity.update(event: TaskAttributeUpdatedEngineEvent,
                      objectMapper: ObjectMapper,
                      limit: Int,
                      filters: List<Pair<JsonPathFilterFunction, FilterType>>) {
  this.taskDefinitionKey = event.taskDefinitionKey
  this.sourceReference = event.sourceReference.toSourceReferenceEmbeddable()
  this.name = event.name ?: this.name
  this.priority = event.priority ?: this.priority
  if (event.correlations.isNotEmpty()) {
    this.correlations.clear()
    this.correlations.addAll(event.correlations.map { entry -> DataEntryId(entryType = entry.key, entryId = "${entry.value}") })
  }
  if (event.payload.isNotEmpty()) {
    this.payload = event.payload.toPayloadJson(objectMapper)
    this.payloadAttributes.clear()
    this.payloadAttributes.addAll(event.payload.toJsonPathsWithValues(limit, filters).map { attr -> PayloadAttribute(attr) }.toMutableSet())
  }
  businessKey = event.businessKey ?: this.businessKey
  description = event.description ?: this.description
  followUpDate = event.followUpDate?.toInstant() ?: this.followUpDate
  dueDate = event.dueDate?.toInstant() ?: this.dueDate
  owner = event.owner ?: this.owner
}

/**
 * Entity to API DTO.
 */
fun TaskEntity.toTask(
  objectMapper: ObjectMapper,
  deleted: Boolean = false
) = Task(
  id = this.taskId,
  sourceReference = this.sourceReference.toSourceReference(),
  taskDefinitionKey = this.taskDefinitionKey,
  payload = this.payload.toPayloadVariableMap(objectMapper),
  correlations = this.correlations.toCorrelations(),
  businessKey = this.businessKey,
  name = this.name,
  description = this.description,
  formKey = this.formKey,
  priority = this.priority,
  createTime = this.createdDate,
  candidateUsers = this.authorizedPrincipals.asUsernames(),
  candidateGroups = this.authorizedPrincipals.asGroupnames(),
  assignee = this.assignee,
  owner = this.owner,
  dueDate = this.dueDate,
  followUpDate = this.followUpDate,
  deleted = deleted
)

/**
 * Create a variable map from stored data entries list.
 */
fun MutableSet<DataEntryId>.toCorrelations(): VariableMap = Variables.fromMap(this.associate { it.entryType to it.entryId })
