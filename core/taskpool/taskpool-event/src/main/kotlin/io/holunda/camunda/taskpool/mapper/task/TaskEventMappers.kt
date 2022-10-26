package io.holunda.camunda.taskpool.mapper.task

import io.holunda.camunda.taskpool.api.task.*
import io.holunda.camunda.taskpool.model.Task
import org.camunda.bpm.engine.variable.VariableMap
import java.util.*

fun Task.createdEvent(): TaskCreatedEngineEvent =
  TaskCreatedEngineEvent(
    id = this.id,
    taskDefinitionKey = this.taskDefinitionKey,
    sourceReference = this.sourceReference,
    formKey = this.formKey,
    name = this.name,
    description = this.description,
    priority = this.priority,
    owner = this.owner,
    dueDate = this.dueDate,
    createTime = this.createTime,
    candidateUsers = this.candidateUsers,
    candidateGroups = this.candidateGroups,
    assignee = this.assignee,
    payload = this.payload,
    correlations = this.correlations,
    businessKey = this.businessKey,
    followUpDate = this.followUpDate
  )

fun Task.assignedEvent(assignee: String?): TaskAssignedEngineEvent =
  TaskAssignedEngineEvent(
    assignee = assignee,
    id = this.id,
    taskDefinitionKey = this.taskDefinitionKey,
    sourceReference = this.sourceReference,
    formKey = this.formKey,
    name = this.name,
    description = this.description,
    priority = this.priority,
    owner = this.owner,
    dueDate = this.dueDate,
    createTime = this.createTime,
    candidateUsers = this.candidateUsers,
    candidateGroups = this.candidateGroups,
    payload = this.payload,
    correlations = this.correlations,
    businessKey = this.businessKey,
    followUpDate = this.followUpDate
  )

fun Task.completedEvent(): TaskCompletedEngineEvent =
  TaskCompletedEngineEvent(
    id = this.id,
    taskDefinitionKey = this.taskDefinitionKey,
    sourceReference = this.sourceReference,
    formKey = this.formKey,
    name = this.name,
    description = this.description,
    priority = this.priority,
    owner = this.owner,
    dueDate = this.dueDate,
    createTime = this.createTime,
    candidateUsers = this.candidateUsers,
    candidateGroups = this.candidateGroups,
    assignee = this.assignee,
    payload = this.payload,
    correlations = this.correlations,
    businessKey = this.businessKey,
    followUpDate = this.followUpDate
  )

fun Task.deletedEvent(deleteReason: String?): TaskDeletedEngineEvent =
  TaskDeletedEngineEvent(
    id = this.id,
    taskDefinitionKey = this.taskDefinitionKey,
    sourceReference = this.sourceReference,
    formKey = this.formKey,
    name = this.name,
    description = this.description,
    priority = this.priority,
    owner = this.owner,
    dueDate = this.dueDate,
    createTime = this.createTime,
    candidateUsers = this.candidateUsers,
    candidateGroups = this.candidateGroups,
    assignee = this.assignee,
    payload = this.payload,
    correlations = this.correlations,
    businessKey = this.businessKey,
    followUpDate = this.followUpDate,
    deleteReason = deleteReason
  )

fun Task.claimedEvent(assignee: String): TaskClaimedEvent =
  TaskClaimedEvent(
    id = this.id,
    taskDefinitionKey = this.taskDefinitionKey,
    sourceReference = this.sourceReference,
    formKey = this.formKey,
    assignee = assignee
  )

fun Task.unclaimedEvent(): TaskUnclaimedEvent =
  TaskUnclaimedEvent(
    id = this.id,
    taskDefinitionKey = this.taskDefinitionKey,
    sourceReference = this.sourceReference,
    formKey = this.formKey
  )

fun Task.deferredEvent(followUpDate: Date): TaskDeferredEvent =
  TaskDeferredEvent(
    id = this.id,
    taskDefinitionKey = this.taskDefinitionKey,
    sourceReference = this.sourceReference,
    formKey = this.formKey,
    followUpDate = followUpDate
  )

fun Task.undeferredEvent(): TaskUndeferredEvent =
  TaskUndeferredEvent(
    id = this.id,
    taskDefinitionKey = this.taskDefinitionKey,
    sourceReference = this.sourceReference,
    formKey = this.formKey
  )

fun Task.markToBeCompletedEvent(payload: VariableMap): TaskToBeCompletedEvent = TaskToBeCompletedEvent(
  id = this.id,
  taskDefinitionKey = this.taskDefinitionKey,
  sourceReference = this.sourceReference,
  formKey = this.formKey,
  payload = payload
)

fun Task.updateAttributesEvent(task: Task, enriched: Boolean): TaskAttributeUpdatedEngineEvent = TaskAttributeUpdatedEngineEvent(
  id = this.id,
  taskDefinitionKey = task.taskDefinitionKey,
  sourceReference = task.sourceReference,
  name = task.name,
  description = task.description,
  priority = task.priority,
  owner = task.owner,
  dueDate = task.dueDate,
  followUpDate = task.followUpDate,
  businessKey = this.businessKey,
  correlations = if (enriched) {
    task.correlations
  } else {
    this.correlations
  },
  payload = if (enriched) {
    task.payload
  } else {
    this.payload
  }
)
