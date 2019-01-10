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

  override val order: Int = ORDER_TASK_ASSIGNMENT,
  override val eventName: String = ASSIGN,

  val assignee: String? = null
) : EngineTaskCommand

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
  override val eventName: String = CREATE,

  override val businessKey: String? = null,
  override val payload: VariableMap = Variables.createVariables(),

  override val correlations: CorrelationMap = newCorrelations(),
  override var enriched: Boolean = false,

  val assignee: String? = null,
  val candidateUsers: Set<String> = setOf(),
  val candidateGroups: Set<String> = setOf(),
  val createTime: Date? = null,
  val description: String? = null,
  val dueDate: Date? = null,
  val followUpDate: Date? = null,
  val name: String? = null,
  val owner: String? = null,
  val priority: Int? = null

) : TaskIdentityWithPayloadAndCorrelations, WithFormKey, EngineTaskCommand

/**
 * Engine command to delete a task.
 */
data class DeleteTaskCommand(
  @TargetAggregateIdentifier
  override val id: String,

  override val order: Int = ORDER_TASK_DELETION,
  override val eventName: String = DELETE,

  val deleteReason: String?
) : EngineTaskCommand

/**
 * Engine command to complete a task.
 */
data class CompleteTaskCommand(
  @TargetAggregateIdentifier
  override val id: String,

  override val order: Int = ORDER_TASK_COMPLETION,
  override val eventName: String = COMPLETE
) : EngineTaskCommand

/**
 * Command to change a task attribute.
 */
data class UpdateAttributeTaskCommand(
  @TargetAggregateIdentifier
  override val id: String,

  override val order: Int = ORDER_TASK_ATTRIBUTE_UPDATE,
  override val eventName: String = ATTRIBUTES,

  val description: String?,
  val dueDate: Date? = null,
  val followUpDate: Date? = null,
  val name: String?,
  val owner: String?,
  val priority: Int?

) : EngineTaskCommand


/**
 * Command to change task assignment.
 */
sealed class UpdateAssignmentTaskCommand(
  @TargetAggregateIdentifier
  override val id: String,
  override val order: Int = ORDER_TASK_CANDIDATES_UPDATE,
  override val eventName: String,
  open val candidateUsers: Set<String>,
  open val candidateGroups: Set<String>
) : EngineTaskCommand

/**
 * Assignment command to add one or more candidate groups.
 */
data class AddCandidateGroupsCommand(
  @TargetAggregateIdentifier
  override val id: String,
  override val candidateGroups: Set<String>
) : UpdateAssignmentTaskCommand(
  id = id,
  eventName = CANDIDATE_GROUP_ADD,
  candidateGroups = candidateGroups,
  candidateUsers = setOf()
)

/**
 * Assignment command to delete one or more candidate groups.
 */
data class DeleteCandidateGroupsCommand(
  @TargetAggregateIdentifier
  override val id: String,
  override val candidateGroups: Set<String> = setOf()
) : UpdateAssignmentTaskCommand(
  id = id,
  eventName = CANDIDATE_GROUP_DELETE,
  candidateGroups = candidateGroups,
  candidateUsers = setOf()
)

/**
 * Assignment command to add one or more candidate users.
 */
data class AddCandidateUsersCommand(
  @TargetAggregateIdentifier
  override val id: String,
  override val candidateUsers: Set<String> = setOf()
) : UpdateAssignmentTaskCommand(
  id = id,
  eventName = CANDIDATE_USER_ADD,
  candidateUsers = candidateUsers,
  candidateGroups = setOf()
)

/**
 * Assignment command to delete one or more candidate users.
 */
data class DeleteCandidateUsersCommand(
  @TargetAggregateIdentifier
  override val id: String,
  override val candidateUsers: Set<String> = setOf()
) : UpdateAssignmentTaskCommand(
  id = id,
  eventName = CANDIDATE_USER_DELETE,
  candidateUsers = candidateUsers,
  candidateGroups = setOf()
)
