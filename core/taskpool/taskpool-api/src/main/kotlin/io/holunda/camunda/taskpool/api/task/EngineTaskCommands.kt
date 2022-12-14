package io.holunda.camunda.taskpool.api.task

import io.holunda.camunda.taskpool.api.business.CorrelationMap
import io.holunda.camunda.taskpool.api.business.newCorrelations
import io.holunda.camunda.taskpool.api.task.CamundaTaskEventType.Companion.ASSIGN
import io.holunda.camunda.taskpool.api.task.CamundaTaskEventType.Companion.ATTRIBUTES
import io.holunda.camunda.taskpool.api.task.CamundaTaskEventType.Companion.BATCH
import io.holunda.camunda.taskpool.api.task.CamundaTaskEventType.Companion.CANDIDATE_GROUP_ADD
import io.holunda.camunda.taskpool.api.task.CamundaTaskEventType.Companion.CANDIDATE_GROUP_DELETE
import io.holunda.camunda.taskpool.api.task.CamundaTaskEventType.Companion.CANDIDATE_USER_ADD
import io.holunda.camunda.taskpool.api.task.CamundaTaskEventType.Companion.CANDIDATE_USER_DELETE
import io.holunda.camunda.taskpool.api.task.CamundaTaskEventType.Companion.COMPLETE
import io.holunda.camunda.taskpool.api.task.CamundaTaskEventType.Companion.CREATE
import io.holunda.camunda.taskpool.api.task.CamundaTaskEventType.Companion.DELETE
import org.axonframework.modelling.command.TargetAggregateIdentifier
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables
import java.util.*

/**
 * Engine command assigning a task.
 */
data class AssignTaskCommand(
  /**
   * User task id.
   */
  @TargetAggregateIdentifier
  override val id: String,
  /**
   * Command order, used for sorting in enricher.
   */
  override val order: Int = ORDER_TASK_ASSIGNMENT,
  /**
   * Command intent name.
   */
  override val eventName: String = ASSIGN,
  /**
   * Username of the assigned user.
   */
  val assignee: String? = null
) : EngineTaskCommand

/**
 * Engine command to create a task.
 */
data class CreateTaskCommand(
  /**
   * User task id.
   */
  @TargetAggregateIdentifier
  override val id: String,
  /**
   * Source reference, indicating why this task exists.
   */
  override val sourceReference: SourceReference,
  /**
   * Task definition key aka task type.
   */
  override val taskDefinitionKey: String,
  /**
   * Form key used to create a task URL.
   */
  override val formKey: String? = null,
  /**
   * Command order used for sorting in collector.
   */
  override val order: Int = ORDER_TASK_CREATION,
  /**
   * Command intent name.
   */
  override val eventName: String = CREATE,
  /**
   * Business key of the underlying process instance.
   */
  override val businessKey: String? = null,
  /**
   * Task payload.
   */
  override val payload: VariableMap = Variables.createVariables(),
  /**
   * Task correlations.
   */
  override val correlations: CorrelationMap = newCorrelations(),
  /**
   * Enrichment flag.
   */
  override var enriched: Boolean = false,
  /**
   * Username of the assigned user.
   */
  val assignee: String? = null,
  /**
   * Set of usernames marked as candidates.
   */
  val candidateUsers: Set<String> = setOf(),
  /**
   * Set of user groups marked as candidates.
   */
  val candidateGroups: Set<String> = setOf(),
  /**
   * Creation timestamp.
   */
  val createTime: Date? = null,
  /**
   * Task description.
   */
  val description: String? = null,
  /**
   * Task due date.
   */
  val dueDate: Date? = null,
  /**
   * Task follow-up date.
   */
  val followUpDate: Date? = null,
  /**
   * Task name.
   */
  val name: String? = null,
  /**
   * Task owner (assigned user).
   */
  val owner: String? = null,
  /**
   * Task priority.
   */
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
  override val eventName: String = COMPLETE,
  /**
   * Username of the assigned user.
   */
  val assignee: String? = null
) : EngineTaskCommand

/**
 * Command to change a task attribute.
 */
data class UpdateAttributeTaskCommand(
  @TargetAggregateIdentifier
  override val id: String,

  override val order: Int = ORDER_TASK_ATTRIBUTE_UPDATE,
  override val eventName: String = ATTRIBUTES,
  /**
   * Source reference, indicating why this task exists.
   */
  override val sourceReference: SourceReference,
  /**
   * Task definition key aka task type.
   */
  override val taskDefinitionKey: String,
  /**
   * Business key of the underlying process instance. Will be ignored, since there is neither a reason to change a business key nor a easy way to get the changed one.
   */
  override val businessKey: String? = null,
  /**
   * Task payload.
   */
  override val payload: VariableMap = Variables.createVariables(),
  /**
   * Task correlations.
   */
  override val correlations: CorrelationMap = newCorrelations(),
  /**
   * Enrichment flag.
   */
  override var enriched: Boolean = false,
  /**
   * Optional task name.
   */
  val description: String?,
  /**
   * Optional task due date.
   */
  val dueDate: Date? = null,
  /**
   * Optional task follow-up date.
   */
  val followUpDate: Date? = null,
  /**
   * Optional task name.
   */
  val name: String?,
  /**
   * Optional task owner (assignee).
   */
  val owner: String?,
  /**
   * Optional task priority.
   */
  val priority: Int?,

) : TaskIdentityWithPayloadAndCorrelations, EngineTaskCommand


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

/**
 * A special command to detect changes originated from TaskListeners.
 * This command has no command handler in the core and is never sent to it.
 * There is currently no way to detect those changes than collecting the history Camunda event and use details to enrich the original intent.
 */
data class UpdateAttributesHistoricTaskCommand(
  @TargetAggregateIdentifier
  override val id: String,

  override val order: Int = ORDER_TASK_HISTORIC_ATTRIBUTE_UPDATE,
  override val eventName: String = CamundaTaskEventType.ATTRIBUTES_LISTENER_UPDATE,
  /**
   * Source reference, indicating why this task exists.
   */
  override val sourceReference: SourceReference,
  /**
   * Task definition key aka task type.
   */
  override val taskDefinitionKey: String,
  /**
   * Business key of the underlying process instance. Will be ignored, since there is neither a reason to change a business key
   * nor an easy way to get the changed one.
   */
  override val businessKey: String? = null,
  /**
   * Task payload.
   */
  override val payload: VariableMap = Variables.createVariables(),
  /**
   * Task correlations.
   */
  override val correlations: CorrelationMap = newCorrelations(),
  /**
   * Enrichment flag.
   */
  override var enriched: Boolean = false,
  /**
   * Optional task name.
   */
  val description: String?,
  /**
   * Optional task due date.
   */
  val dueDate: Date? = null,
  /**
   * Optional task follow-up date.
   */
  val followUpDate: Date? = null,
  /**
   * Optional task name.
   */
  val name: String?,
  /**
   * Optional task owner (assignee).
   */
  val owner: String?,
  /**
   * Optional task priority.
   */
  val priority: Int?,

  ) : TaskIdentityWithPayloadAndCorrelations, EngineTaskCommand
