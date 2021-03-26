package io.holunda.camunda.taskpool.view

import io.holunda.camunda.taskpool.api.business.*
import io.holunda.camunda.taskpool.api.task.*
import io.holunda.camunda.taskpool.api.task.WithPayload
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables
import java.util.*

/**
 * User task.
 */
data class Task(
  override val id: String,
  override val sourceReference: SourceReference,
  override val taskDefinitionKey: String,
  override val payload: VariableMap = Variables.createVariables(),
  override val correlations: CorrelationMap = newCorrelations(),
  override val businessKey: String? = null,
  val name: String? = null,
  val description: String? = null,
  val formKey: String? = null,
  val priority: Int? = 0,
  val createTime: Date? = null,
  val candidateUsers: Set<String> = setOf(),
  val candidateGroups: Set<String> = setOf(),
  val assignee: String? = null,
  val owner: String? = null,
  val dueDate: Date? = null,
  val followUpDate: Date? = null,
  val deleted: Boolean = false
) : TaskIdentity, WithPayload, WithCorrelations {

  val correlationIdentities by lazy {
    correlations.map { dataIdentityString(it.key, it.value as EntryId) }.toSet()
  }

}

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
  createTime = event.createTime,
  assignee = event.assignee,
  candidateGroups = event.candidateGroups,
  candidateUsers = event.candidateUsers,

  priority = event.priority,
  name = event.name,
  description = event.description,
  owner = event.owner,
  followUpDate = task.followUpDate,
  dueDate = event.dueDate
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
  createTime = event.createTime,
  assignee = event.assignee,
  candidateGroups = event.candidateGroups,
  candidateUsers = event.candidateUsers,

  name = event.name,
  description = event.description,
  priority = event.priority,
  owner = event.owner,
  followUpDate = event.followUpDate,
  dueDate = event.dueDate
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
  businessKey = event.businessKey ?: task.businessKey,
  priority = event.priority,
  owner = event.owner,
  followUpDate = event.followUpDate,
  dueDate = event.dueDate,
  correlations = event.correlations,
  payload = event.payload

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
