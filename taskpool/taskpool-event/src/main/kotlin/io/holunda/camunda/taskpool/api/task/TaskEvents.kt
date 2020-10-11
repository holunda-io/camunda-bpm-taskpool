package io.holunda.camunda.taskpool.api.task

import io.holunda.camunda.taskpool.api.business.CorrelationMap
import io.holunda.camunda.taskpool.api.business.WithCorrelations
import io.holunda.camunda.taskpool.api.business.newCorrelations
import org.axonframework.serialization.Revision
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables
import java.util.*

/**
 * Task event is either:
 * <ul>
 *     <li>TaskEngineEvent</li>
 *     <li>TaskAttributeUpdateEvent</li>
 *     <li>TaskAssignmentUpdateEvent</li>
 *     <li>TaskInteractionEvent</li>
 * </ul>
 */
sealed class TaskEvent(val eventType: String) : TaskIdentity

/**
 * Event informing about changes in the engine.
 * @param eventType type of event.
 */
sealed class TaskEngineEvent(eventType: String) : TaskEvent(eventType), WithPayload, WithCorrelations

/**
 * Event informing about changes by the user interaction.
 * @param eventType type of event.
 */
sealed class TaskInteractionEvent(eventType: String) : TaskEvent(eventType), WithFormKey

/**
 * Task created.
 */
@Revision("4")
data class TaskCreatedEngineEvent(
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
  val followUpDate: Date? = null
) : TaskEngineEvent("create")

/**
 * Task assigned to user.
 */
@Revision("4")
data class TaskAssignedEngineEvent(
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
  val followUpDate: Date? = null
) : TaskEngineEvent("assign")

/**
 * Task completed.
 */
@Revision("4")
data class TaskCompletedEngineEvent(
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
  val followUpDate: Date? = null
) : TaskEngineEvent("complete")

/**
 * Task deleted (happenc on instance cancellation).
 */
@Revision("4")
data class TaskDeletedEngineEvent(
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
  val deleteReason: String?
) : TaskEngineEvent("delete")

/**
 * Changes of task attributes (but not assingment).
 */
@Revision("4")
data class TaskAttributeUpdatedEngineEvent(
  override val id: String,
  override val sourceReference: SourceReference,
  override val taskDefinitionKey: String,
  val name: String? = null,
  val description: String? = null,
  val priority: Int? = 0,
  val owner: String? = null,
  val dueDate: Date? = null,
  val followUpDate: Date? = null
) : TaskEvent("attribute-update")

/**
 * Changes of task assignment.
 */
@Revision("4")
sealed class TaskAssignmentUpdatedEngineEvent(
  override val id: String,
  override val sourceReference: SourceReference,
  override val taskDefinitionKey: String,
  assignmentUpdateType: String
) : TaskEvent(assignmentUpdateType)

/**
 * Change of a candidate group.
 */
@Revision("4")
data class TaskCandidateGroupChanged(
  override val id: String,
  override val sourceReference: SourceReference,
  override val taskDefinitionKey: String,
  val groupId: String,
  val assignmentUpdateType: String
) : TaskAssignmentUpdatedEngineEvent(id, sourceReference, taskDefinitionKey, assignmentUpdateType)

/**
 * Change of candidate user.
 */
@Revision("4")
data class TaskCandidateUserChanged(
  override val id: String,
  override val sourceReference: SourceReference,
  override val taskDefinitionKey: String,
  val userId: String,
  val assignmentUpdateType: String
) : TaskAssignmentUpdatedEngineEvent(id, sourceReference, taskDefinitionKey, assignmentUpdateType)

/**
 * Claim executed by the user. Task engine listens to this event to change the assignee.
 */
@Revision("4")
data class TaskClaimedEvent(
  override val id: String,
  override val sourceReference: SourceReference,
  override val taskDefinitionKey: String,
  override val formKey: String?,
  val assignee: String
) : TaskInteractionEvent("claim")

/**
 * Un-claim executed by the user. Task engine listens to this event to change the assignee.
 */
@Revision("4")
data class TaskUnclaimedEvent(
  override val id: String,
  override val sourceReference: SourceReference,
  override val taskDefinitionKey: String,
  override val formKey: String?
) : TaskInteractionEvent("unclaim")

/**
 * Complete by the user. Task engine listens to this event to complete the task.
 */
@Revision("4")
data class TaskToBeCompletedEvent(
  override val id: String,
  override val sourceReference: SourceReference,
  override val taskDefinitionKey: String,
  override val formKey: String?,
  val payload: VariableMap = Variables.createVariables()
) : TaskInteractionEvent("mark-complete")

/**
 * Defer by the user. Task engine listens to this event to change the follow-up date.
 */
@Revision("4")
data class TaskDeferredEvent(
  override val id: String,
  override val sourceReference: SourceReference,
  override val taskDefinitionKey: String,
  override val formKey: String?,
  val followUpDate: Date
) : TaskInteractionEvent("defer")

/**
 * Undefer by the user. Task engine listens to this event to change the follow-up date.
 */
@Revision("4")
data class TaskUndeferredEvent(
  override val id: String,
  override val sourceReference: SourceReference,
  override val taskDefinitionKey: String,
  override val formKey: String?
) : TaskInteractionEvent("undefer")

