package io.holunda.camunda.taskpool.api.task

import io.holunda.camunda.taskpool.api.business.CorrelationMap
import io.holunda.camunda.taskpool.api.business.newCorrelations
import org.axonframework.modelling.command.TargetAggregateIdentifier
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables
import java.util.*

data class AssignTaskCommand(
  @TargetAggregateIdentifier
  override val id: String,
  override val taskDefinitionKey: String,
  override val sourceReference: SourceReference,

  override val name: String? = null,
  override val description: String? = null,
  override val priority: Int? = 0,
  override val createTime: Date? = null,
  override val owner: String? = null,
  override val eventName: String = "assign",
  override val candidateUsers: List<String> = listOf(),
  override val candidateGroups: List<String> = listOf(),
  override val assignee: String? = null,
  override val dueDate: Date? = null,
  override val followUpDate: Date? = null,
  override val formKey: String? = null,
  override val businessKey: String? = null,
  override val payload: VariableMap = Variables.createVariables(),
  override val correlations: CorrelationMap = newCorrelations(),
  override var enriched: Boolean = false
) : EnrichedEngineTaskCommand


data class CreateTaskCommand(
  @TargetAggregateIdentifier
  override val id: String,
  override val sourceReference: SourceReference,
  override val taskDefinitionKey: String,

  override val name: String? = null,
  override val description: String? = null,
  override val priority: Int? = 0,
  override val createTime: Date? = null,
  override val owner: String? = null,
  override val eventName: String = "create",
  override val candidateUsers: List<String> = listOf(),
  override val candidateGroups: List<String> = listOf(),
  override val assignee: String? = null,
  override val dueDate: Date? = null,
  override val followUpDate: Date? = null,
  override val formKey: String? = null,
  override val businessKey: String? = null,
  override val payload: VariableMap = Variables.createVariables(),
  override val correlations: CorrelationMap = newCorrelations(),
  override var enriched: Boolean = false
) : EnrichedEngineTaskCommand

data class DeleteTaskCommand(
  @TargetAggregateIdentifier
  override val id: String,
  override val taskDefinitionKey: String,
  override val sourceReference: SourceReference,

  override val name: String? = null,
  override val description: String? = null,
  override val priority: Int? = 0,
  override val createTime: Date? = null,
  override val owner: String? = null,
  override val eventName: String = "delete",
  override val candidateUsers: List<String> = listOf(),
  override val candidateGroups: List<String> = listOf(),
  override val assignee: String? = null,
  override val dueDate: Date? = null,
  override val followUpDate: Date? = null,
  override val formKey: String? = null,
  override val businessKey: String? = null,
  override val payload: VariableMap = Variables.createVariables(),
  override val correlations: CorrelationMap = newCorrelations(),
  override var enriched: Boolean = false,
  val deleteReason: String?
) : EnrichedEngineTaskCommand

data class CompleteTaskCommand(
  @TargetAggregateIdentifier
  override val id: String,
  override val sourceReference: SourceReference,
  override val taskDefinitionKey: String,

  override val name: String? = null,
  override val description: String? = null,
  override val priority: Int? = 0,
  override val createTime: Date? = null,
  override val owner: String? = null,
  override val eventName: String = "complete",
  override val candidateUsers: List<String> = listOf(),
  override val candidateGroups: List<String> = listOf(),
  override val assignee: String? = null,
  override val dueDate: Date? = null,
  override val followUpDate: Date? = null,
  override val formKey: String? = null,
  override val businessKey: String? = null,
  override val payload: VariableMap = Variables.createVariables(),
  override val correlations: CorrelationMap = newCorrelations(),
  override var enriched: Boolean = false

) : EnrichedEngineTaskCommand

data class CreateOrAssignTaskCommand(
  @TargetAggregateIdentifier
  override val id: String,
  override val taskDefinitionKey: String,
  override val sourceReference: SourceReference,
  override val eventName: String,

  override val name: String? = null,
  override val description: String? = null,
  override val priority: Int? = 0,
  override val createTime: Date? = null,
  override val candidateUsers: List<String> = listOf(),
  override val candidateGroups: List<String> = listOf(),
  override val assignee: String? = null,
  override val owner: String? = null,
  override val dueDate: Date? = null,
  override val followUpDate: Date? = null,
  override val formKey: String? = null,
  override val businessKey: String? = null,
  override val payload: VariableMap = Variables.createVariables(),
  override val correlations: CorrelationMap = newCorrelations(),
  override var enriched: Boolean = false
) : EnrichedEngineTaskCommand


sealed class UpdateTaskCommand(
  @TargetAggregateIdentifier
  override val id: String,
  override val sourceReference: SourceReference,
  override val taskDefinitionKey: String,

  override val name: String? = null,
  override val description: String? = null,
  override val priority: Int? = 0,
  override val createTime: Date? = null,
  override val owner: String? = null,
  override val eventName: String = "update",
  override val candidateUsers: List<String> = listOf(),
  override val candidateGroups: List<String> = listOf(),
  override val assignee: String? = null,
  override val dueDate: Date? = null,
  override val followUpDate: Date? = null,
  override val formKey: String? = null
) : EngineTaskCommand

data class AttributeUpdateTaskCommand(
  @TargetAggregateIdentifier
  override val id: String,
  override val sourceReference: SourceReference,
  override val taskDefinitionKey: String,
  override val name: String?,
  override val description: String?,
  override val priority: Int?,
  override val assignee: String?,
  override val owner: String?,
  override val dueDate: Date? = null,
  override val followUpDate: Date? = null

) : UpdateTaskCommand(id, sourceReference, taskDefinitionKey,
  name = name,
  description = description,
  priority = priority,
  assignee = assignee,
  owner = owner,
  dueDate = dueDate,
  followUpDate = followUpDate,
  eventName = "attribute-update"
)

sealed class AssignmentUpdateTaskCommand(
  override val id: String,
  override val sourceReference: SourceReference,
  override val taskDefinitionKey: String,
  override val eventName: String = "assignment-update",
  open val userId: String?,
  open val groupId: String?
) : UpdateTaskCommand(id, sourceReference, taskDefinitionKey,
  eventName = "assignment-update"
)

data class CandidateGroupAddCommand(
  override val id: String,
  override val sourceReference: SourceReference,
  override val taskDefinitionKey: String,
  override val groupId: String
) : AssignmentUpdateTaskCommand(id, sourceReference, taskDefinitionKey,
  eventName = "candidate-group-add",
  groupId = groupId,
  userId = null
)

data class CandidateGroupDeleteCommand(
  override val id: String,
  override val sourceReference: SourceReference,
  override val taskDefinitionKey: String,
  override val groupId: String
) : AssignmentUpdateTaskCommand(id, sourceReference, taskDefinitionKey,
  eventName = "candidate-group-delete",
  groupId = groupId,
  userId = null
)


data class CandidateUserAddCommand(
  override val id: String,
  override val sourceReference: SourceReference,
  override val taskDefinitionKey: String,
  override val userId: String
) : AssignmentUpdateTaskCommand(id, sourceReference, taskDefinitionKey,
  eventName = "candidate-user-add",
  groupId = null,
  userId = userId
)

data class CandidateUserDeleteCommand(
  override val id: String,
  override val sourceReference: SourceReference,
  override val taskDefinitionKey: String,
  override val userId: String
) : AssignmentUpdateTaskCommand(id, sourceReference, taskDefinitionKey,
  eventName = "candidate-user-delete",
  groupId = null,
  userId = userId
)
