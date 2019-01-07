package io.holunda.camunda.taskpool.api.task

import io.holunda.camunda.taskpool.api.business.CorrelationMap
import io.holunda.camunda.taskpool.api.business.newCorrelations
import io.holunda.camunda.taskpool.api.task.CamundaTaskEvent.Companion.ASSIGN
import io.holunda.camunda.taskpool.api.task.CamundaTaskEvent.Companion.ATTRIBUTES
import io.holunda.camunda.taskpool.api.task.CamundaTaskEvent.Companion.CANDIDATE_GROUP_ADD
import io.holunda.camunda.taskpool.api.task.CamundaTaskEvent.Companion.CANDIDATE_GROUP_DELETE
import io.holunda.camunda.taskpool.api.task.CamundaTaskEvent.Companion.CANDIDATE_USER_ADD
import io.holunda.camunda.taskpool.api.task.CamundaTaskEvent.Companion.CANDIDATE_USER_DELETE
import io.holunda.camunda.taskpool.api.task.CamundaTaskEvent.Companion.COMPLETE
import io.holunda.camunda.taskpool.api.task.CamundaTaskEvent.Companion.CREATE
import io.holunda.camunda.taskpool.api.task.CamundaTaskEvent.Companion.DELETE
import org.axonframework.modelling.command.TargetAggregateIdentifier
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables
import java.util.*

/**
 * Engine command assigning a task.
 */
data class AssignTaskCommand(
  @TargetAggregateIdentifier
  override val id: String,
  override val sourceReference: SourceReference,
  override val taskDefinitionKey: String,

  override val order: Int = ORDER_TASK_UPDATE,
  override val name: String? = null,
  override val description: String? = null,
  override val priority: Int? = 50,
  override val createTime: Date? = null,
  override val owner: String? = null,
  override val eventName: String = ASSIGN,
  override val candidateUsers: List<String> = listOf(),
  override val candidateGroups: List<String> = listOf(),
  override val assignee: String? = null,
  override val dueDate: Date? = null,
  override val followUpDate: Date? = null,
  override val businessKey: String? = null,
  override val payload: VariableMap = Variables.createVariables(),
  override val correlations: CorrelationMap = newCorrelations(),
  override var enriched: Boolean = false
) : EnrichedEngineTaskCommand

/**
 * Engine command to create a task.
 */
data class CreateTaskCommand(
  @TargetAggregateIdentifier
  override val id: String,
  override val sourceReference: SourceReference,
  override val taskDefinitionKey: String,
  override val formKey: String? = null,

  override val order: Int = ORDER_TASK_CREATION,
  override val name: String? = null,
  override val description: String? = null,
  override val priority: Int? = 50,
  override val createTime: Date? = null,
  override val owner: String? = null,
  override val eventName: String = CREATE,
  override val candidateUsers: List<String> = listOf(),
  override val candidateGroups: List<String> = listOf(),
  override val assignee: String? = null,
  override val dueDate: Date? = null,
  override val followUpDate: Date? = null,
  override val businessKey: String? = null,
  override val payload: VariableMap = Variables.createVariables(),
  override val correlations: CorrelationMap = newCorrelations(),
  override var enriched: Boolean = false
) : EnrichedEngineTaskCommand, WithFormKey, WithTaskId

/**
 * Engine command to delete a task.
 */
data class DeleteTaskCommand(
  @TargetAggregateIdentifier
  override val id: String,
  override val sourceReference: SourceReference,
  override val taskDefinitionKey: String,

  override val order: Int = ORDER_TASK_DELETION,
  override val name: String? = null,
  override val description: String? = null,
  override val priority: Int? = 50,
  override val createTime: Date? = null,
  override val owner: String? = null,
  override val eventName: String = DELETE,
  override val candidateUsers: List<String> = listOf(),
  override val candidateGroups: List<String> = listOf(),
  override val assignee: String? = null,
  override val dueDate: Date? = null,
  override val followUpDate: Date? = null,
  override val businessKey: String? = null,
  override val payload: VariableMap = Variables.createVariables(),
  override val correlations: CorrelationMap = newCorrelations(),
  override var enriched: Boolean = false,
  val deleteReason: String?
) : EnrichedEngineTaskCommand

/**
 * Engine command to complete a task.
 */
data class CompleteTaskCommand(
  @TargetAggregateIdentifier
  override val id: String,
  override val sourceReference: SourceReference,
  override val taskDefinitionKey: String,

  override val order: Int = ORDER_TASK_COMPLETION,
  override val name: String? = null,
  override val description: String? = null,
  override val priority: Int? = 50,
  override val createTime: Date? = null,
  override val owner: String? = null,
  override val eventName: String = COMPLETE,
  override val candidateUsers: List<String> = listOf(),
  override val candidateGroups: List<String> = listOf(),
  override val assignee: String? = null,
  override val dueDate: Date? = null,
  override val followUpDate: Date? = null,
  override val businessKey: String? = null,
  override val payload: VariableMap = Variables.createVariables(),
  override val correlations: CorrelationMap = newCorrelations(),
  override var enriched: Boolean = false

) : EnrichedEngineTaskCommand

/**
 * Command to update existing task.
 */
sealed class UpdateTaskCommand(
  @TargetAggregateIdentifier
  override val id: String,

  override val order: Int = ORDER_TASK_UPDATE,
  override val name: String? = null,
  override val description: String? = null,
  override val priority: Int? = 50,
  override val createTime: Date? = null,
  override val owner: String? = null,
  override val eventName: String,
  override val candidateUsers: List<String> = listOf(),
  override val candidateGroups: List<String> = listOf(),
  override val assignee: String? = null,
  override val dueDate: Date? = null,
  override val followUpDate: Date? = null
) : EngineTaskCommand

/**
 * Command to change a task attribute.
 */
data class UpdateAttributeTaskCommand(
  @TargetAggregateIdentifier
  override val id: String,
  override val sourceReference: SourceReference,
  override val taskDefinitionKey: String,

  override val order: Int = ORDER_TASK_UPDATE,
  override val name: String?,
  override val description: String?,
  override val priority: Int?,
  override val assignee: String?,
  override val owner: String?,
  override val dueDate: Date? = null,
  override val followUpDate: Date? = null

) : UpdateTaskCommand(id,
  name = name,
  description = description,
  priority = priority,
  assignee = assignee,
  owner = owner,
  dueDate = dueDate,
  followUpDate = followUpDate,
  eventName = ATTRIBUTES
), TaskIdentity

/**
 * Command to change task assignment.
 */
sealed class UpdateAssignmentTaskCommand(
  override val id: String,
  override val eventName: String,
  open val userId: String?,
  open val groupId: String?
) : UpdateTaskCommand(
  id = id,
  eventName = eventName
)

/**
 * Assignment command to add a candidate group.
 */
data class AddCandidateGroupCommand(
  override val id: String,
  override val groupId: String
) : UpdateAssignmentTaskCommand(
  id = id,
  eventName = CANDIDATE_GROUP_ADD,
  groupId = groupId,
  userId = null
)

/**
 * Assignment command to delete a candidate group.
 */
data class DeleteCandidateGroupCommand(
  override val id: String,
  override val groupId: String
) : UpdateAssignmentTaskCommand(
  id = id,
  eventName = CANDIDATE_GROUP_DELETE,
  groupId = groupId,
  userId = null
)

/**
 * Assignment command to add a candidate user.
 */
data class AddCandidateUserCommand(
  override val id: String,
  override val userId: String
) : UpdateAssignmentTaskCommand(
  id = id,
  eventName = CANDIDATE_USER_ADD,
  groupId = null,
  userId = userId
)

/**
 * Assignment command to delete a candidate user.
 */
data class DeleteCandidateUserCommand(
  override val id: String,
  override val userId: String
) : UpdateAssignmentTaskCommand(
  id = id,
  eventName = CANDIDATE_USER_DELETE,
  groupId = null,
  userId = userId
)

// defines a partial order on EngineTaskCommand
const val ORDER_TASK_CREATION = Int.MIN_VALUE
const val ORDER_TASK_DELETION = Int.MAX_VALUE
const val ORDER_TASK_COMPLETION = ORDER_TASK_DELETION - 1
const val ORDER_TASK_UPDATE = 0
