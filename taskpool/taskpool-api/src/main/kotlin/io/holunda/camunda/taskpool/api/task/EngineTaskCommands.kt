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
  override val formKey: String? = null,
  override val businessKey: String? = null,
  override val payload: VariableMap = Variables.createVariables(),
  override val correlations: CorrelationMap = newCorrelations(),
  override var enriched: Boolean = false
) : EngineTaskCommand


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
  override val formKey: String? = null,
  override val businessKey: String? = null,
  override val payload: VariableMap = Variables.createVariables(),
  override val correlations: CorrelationMap = newCorrelations(),
  override var enriched: Boolean = false
) : EngineTaskCommand

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
  override val formKey: String? = null,
  override val businessKey: String? = null,
  override val payload: VariableMap = Variables.createVariables(),
  override val correlations: CorrelationMap = newCorrelations(),
  override var enriched: Boolean = false,
  val deleteReason: String?
) : EngineTaskCommand

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
  override val formKey: String? = null,
  override val businessKey: String? = null,
  override val payload: VariableMap = Variables.createVariables(),
  override val correlations: CorrelationMap = newCorrelations(),
  override var enriched: Boolean = false

) : EngineTaskCommand



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
  override val formKey: String? = null,
  override val businessKey: String? = null,
  override val payload: VariableMap = Variables.createVariables(),
  override val correlations: CorrelationMap = newCorrelations(),
  override var enriched: Boolean = false
) : EngineTaskCommand


