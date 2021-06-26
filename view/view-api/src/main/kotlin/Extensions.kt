package io.holunda.polyflow.view

import io.holunda.camunda.taskpool.api.business.DataEntryState
import io.holunda.camunda.taskpool.api.business.Modification
import io.holunda.camunda.taskpool.api.task.*
import java.util.*

/**
 * Create a new task from data of existing task and incoming event.
 * @param event to construct from.
 * @param task existing task.
 * @return new task with merged values.
 */
fun task(event: TaskAssignedEngineEvent, task: Task) = Task(
  id = event.id,
  sourceReference = event.sourceReference,
  taskDefinitionKey = event.taskDefinitionKey,

  correlations = task.correlations,
  payload = task.payload,
  businessKey = event.businessKey,
  formKey = task.formKey,
  createTime = event.createTime?.toInstant(),
  assignee = event.assignee,
  candidateGroups = event.candidateGroups,
  candidateUsers = event.candidateUsers,

  priority = event.priority,
  name = event.name,
  description = event.description,
  owner = event.owner,
  followUpDate = task.followUpDate,
  dueDate = event.dueDate?.toInstant()
)

/**
 * Create a new task from data of incoming event.
 * @param event to construct from.
 * @return new task.
 */
fun task(event: TaskCreatedEngineEvent) = Task(
  id = event.id,
  sourceReference = event.sourceReference,
  taskDefinitionKey = event.taskDefinitionKey,

  correlations = event.correlations,
  payload = event.payload,
  businessKey = event.businessKey,
  formKey = event.formKey,
  createTime = event.createTime?.toInstant(),
  assignee = event.assignee,
  candidateGroups = event.candidateGroups,
  candidateUsers = event.candidateUsers,

  name = event.name,
  description = event.description,
  priority = event.priority,
  owner = event.owner,
  followUpDate = event.followUpDate?.toInstant(),
  dueDate = event.dueDate?.toInstant()
)

/**
 * Create a new task from data of existing task and incoming event.
 * @param event to construct from.
 * @param task existing task.
 * @return new task with merged values.
 */
fun task(event: TaskAttributeUpdatedEngineEvent, task: Task) = Task(
  id = event.id,
  sourceReference = event.sourceReference,
  taskDefinitionKey = event.taskDefinitionKey,

  formKey = task.formKey,
  createTime = task.createTime,
  assignee = task.assignee,
  candidateGroups = task.candidateGroups,
  candidateUsers = task.candidateUsers,

  name = event.name,
  description = event.description,
  priority = event.priority,
  owner = event.owner,
  followUpDate = event.followUpDate?.toInstant(),
  dueDate = event.dueDate?.toInstant(),

  businessKey = event.businessKey ?: task.businessKey,
  correlations = if (event.correlations.isEmpty()) {
    task.correlations
  } else {
    event.correlations
  },
  payload = if (event.payload.isEmpty()) {
    task.payload
  } else {
    event.payload
  }

)

/**
 * Create a new task from data of existing task and incoming event.
 * @param event to construct from.
 * @param task existing task.
 * @return new task with merged values.
 */
fun task(event: TaskCandidateGroupChanged, task: Task) = Task(
  id = event.id,
  sourceReference = event.sourceReference,
  taskDefinitionKey = event.taskDefinitionKey,

  correlations = task.correlations,
  payload = task.payload,
  businessKey = task.businessKey,
  formKey = task.formKey,
  createTime = task.createTime,
  assignee = task.assignee,

  candidateGroups = when (event.assignmentUpdateType) {
    CamundaTaskEventType.CANDIDATE_GROUP_ADD -> task.candidateGroups.plus(event.groupId)
    CamundaTaskEventType.CANDIDATE_GROUP_DELETE -> task.candidateGroups.minus(event.groupId)
    else -> task.candidateGroups
  },
  candidateUsers = task.candidateUsers,

  name = task.name,
  description = task.description,
  priority = task.priority,
  owner = task.owner,
  followUpDate = task.followUpDate,
  dueDate = task.dueDate
)

/**
 * Create a new task from data of existing task and incoming event.
 * @param event to construct from.
 * @param task existing task.
 * @return new task with merged values.
 */
fun task(event: TaskCandidateUserChanged, task: Task) = Task(
  id = event.id,
  sourceReference = event.sourceReference,
  taskDefinitionKey = event.taskDefinitionKey,

  correlations = task.correlations,
  payload = task.payload,
  businessKey = task.businessKey,
  formKey = task.formKey,
  createTime = task.createTime,
  assignee = task.assignee,

  candidateGroups = task.candidateGroups,
  candidateUsers = when (event.assignmentUpdateType) {
    CamundaTaskEventType.CANDIDATE_USER_ADD -> task.candidateUsers.plus(event.userId)
    CamundaTaskEventType.CANDIDATE_USER_DELETE -> task.candidateUsers.minus(event.userId)
    else -> task.candidateUsers
  },

  name = task.name,
  description = task.description,
  priority = task.priority,
  owner = task.owner,
  followUpDate = task.followUpDate,
  dueDate = task.dueDate
)

/**
 * Adds modification to the list of protocol entry list.
 */
fun addModification(modifications: List<ProtocolEntry>, modification: Modification, state: DataEntryState) =
  modifications.plus(ProtocolEntry(
    time = modification.time.toInstant(),
    username = modification.username,
    logMessage = modification.log,
    logDetails = modification.logNotes,
    state = state
  ))
