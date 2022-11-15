package io.holunda.camunda.taskpool.mapper.task

import io.holunda.camunda.taskpool.api.task.*
import io.holunda.camunda.taskpool.model.Task
import org.camunda.bpm.engine.variable.VariableMap
import java.util.*

/**
 * Creates event filled with data from the task.
 * @return event.
 */
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

/**
 * Creates event filled with data from the task and additional values passed in.
 * @param assignee new assignee.
 * @return event.
 */
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

/**
 * Creates event filled with data from the task.
 * @return event.
 */
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

/**
 * Creates event filled with data from the task and additional values passed in.
 * @param deleteReason reason for task deletion.
 * @return event.
 */
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

/**
 * Creates event filled with data from the task and additional values passed in.
 * @param assignee user claiming the task.
 * @return event.
 */
fun Task.claimedEvent(assignee: String): TaskClaimedEvent =
  TaskClaimedEvent(
    id = this.id,
    taskDefinitionKey = this.taskDefinitionKey,
    sourceReference = this.sourceReference,
    formKey = this.formKey,
    assignee = assignee
  )

/**
 * Creates event filled with data from the task.
 * @return event.
 */
fun Task.unclaimedEvent(): TaskUnclaimedEvent =
  TaskUnclaimedEvent(
    id = this.id,
    taskDefinitionKey = this.taskDefinitionKey,
    sourceReference = this.sourceReference,
    formKey = this.formKey
  )

/**
 * Creates event filled with data from the task and additional values passed in.
 * @param followUpDate time to defer until.
 * @return event.
 */
fun Task.deferredEvent(followUpDate: Date): TaskDeferredEvent =
  TaskDeferredEvent(
    id = this.id,
    taskDefinitionKey = this.taskDefinitionKey,
    sourceReference = this.sourceReference,
    formKey = this.formKey,
    followUpDate = followUpDate
  )

/**
 * Creates event filled with data from the task.
 * @return event.
 */
fun Task.undeferredEvent(): TaskUndeferredEvent =
  TaskUndeferredEvent(
    id = this.id,
    taskDefinitionKey = this.taskDefinitionKey,
    sourceReference = this.sourceReference,
    formKey = this.formKey
  )

/**
 * Creates event filled with data from the task and additional values passed in.
 * @param payload variables to set on completion.
 * @return event.
 */
fun Task.markToBeCompletedEvent(payload: VariableMap): TaskToBeCompletedEvent = TaskToBeCompletedEvent(
  id = this.id,
  taskDefinitionKey = this.taskDefinitionKey,
  sourceReference = this.sourceReference,
  formKey = this.formKey,
  payload = payload
)

/**
 * Creates event filled with data from the task and additional values passed in.
 * @param task task containing new attributes.
 * @param enriched flag indicating if the task carries new payload and correlations to replace existing.
 * @return event.
 */
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
  businessKey = if (task.businessKey != null) {
    task.businessKey
  } else {
    this.businessKey
  },
  formKey = if (task.formKey != null) {
    task.formKey
  } else {
    this.formKey
  },
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

/**
 * Creates event filled with data from the task and additional values passed in.
 * @param groupId group to add to candidate group list.
 * @return event.
 */
fun Task.addCandidateGroupEvent(groupId: String): TaskCandidateGroupChanged = TaskCandidateGroupChanged(
  id = this.id,
  taskDefinitionKey = this.taskDefinitionKey,
  sourceReference = this.sourceReference,
  groupId = groupId,
  assignmentUpdateType = CamundaTaskEventType.CANDIDATE_GROUP_ADD
)

/**
 * Creates event filled with data from the task and additional values passed in.
 * @param groupId group to remove from candidate group list.
 * @return event.
 */
fun Task.removeCandidateGroupEvent(groupId: String): TaskCandidateGroupChanged = TaskCandidateGroupChanged(
  id = this.id,
  taskDefinitionKey = this.taskDefinitionKey,
  sourceReference = this.sourceReference,
  groupId = groupId,
  assignmentUpdateType = CamundaTaskEventType.CANDIDATE_GROUP_DELETE
)

/**
 * Creates event filled with data from the task and additional values passed in.
 * @param userId user to add to candidate user list.
 * @return event.
 */
fun Task.addCandidateUserEvent(userId: String): TaskCandidateUserChanged = TaskCandidateUserChanged(
  id = this.id,
  taskDefinitionKey = this.taskDefinitionKey,
  sourceReference = this.sourceReference,
  userId = userId,
  assignmentUpdateType = CamundaTaskEventType.CANDIDATE_USER_ADD
)

/**
 * Creates event filled with data from the task and additional values passed in.
 * @param userId user to remove from candidate user list.
 * @return event.
 */
fun Task.removeCandidateUserEvent(userId: String): TaskCandidateUserChanged = TaskCandidateUserChanged(
  id = this.id,
  taskDefinitionKey = this.taskDefinitionKey,
  sourceReference = this.sourceReference,
  userId = userId,
  assignmentUpdateType = CamundaTaskEventType.CANDIDATE_USER_DELETE
)
