package io.holunda.camunda.taskpool.api.task

import io.holunda.camunda.taskpool.api.business.CorrelationMap
import io.holunda.camunda.taskpool.api.business.newCorrelations
import org.axonframework.commandhandling.TargetAggregateIdentifier
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables
import java.util.*

data class AssignTaskCommand(
  @TargetAggregateIdentifier
  override val id: String,
  override val name: String?,
  override val description: String?,
  override val priority: Int?,
  override val processReference: ProcessReference?,
  override val caseReference: CaseReference?,
  override val createTime: Date?,
  override val taskDefinitionKey: String,
  override val eventName: String = "assignment",
  override val candidateUsers: List<String>,
  override val candidateGroups: List<String>,
  override val assignee: String?,
  override val owner: String?,
  override val dueDate: Date?,
  override val formKey: String?,
  override val businessKey: String?,
  override val payload: VariableMap = Variables.createVariables(),
  override val correlations: CorrelationMap = newCorrelations(),
  override var enriched: Boolean = false
) : TaskCommand


data class CreateTaskCommand(
  @TargetAggregateIdentifier
  override val id: String,
  override val name: String?,
  override val description: String? = null,
  override val priority: Int? = 50,
  override val processReference: ProcessReference? = null,
  override val caseReference: CaseReference? = null,
  override val createTime: Date?,
  override val owner: String?,
  override val taskDefinitionKey: String,
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
) : TaskCommand

data class DeleteTaskCommand(
  @TargetAggregateIdentifier
  override val id: String,
  override val name: String? = null,
  override val description: String? = null,
  override val priority: Int? = 50,
  override val processReference: ProcessReference? = null,
  override val caseReference: CaseReference? = null,
  override val createTime: Date? = null,
  override val owner: String? = null,
  override val taskDefinitionKey: String,
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
) : TaskCommand

data class CompleteTaskCommand(
  @TargetAggregateIdentifier
  override val id: String,
  override val name: String?,
  override val description: String? = null,
  override val priority: Int? = 50,
  override val processReference: ProcessReference? = null,
  override val caseReference: CaseReference? = null,
  override val createTime: Date?,
  override val owner: String?,
  override val taskDefinitionKey: String,
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

) : TaskCommand

data class CreateOrAssignTaskCommand(
  @TargetAggregateIdentifier
  override val id: String,
  override val name: String?,
  override val description: String?,
  override val priority: Int?,
  override val processReference: ProcessReference? = null,
  override val caseReference: CaseReference? = null,
  override val createTime: Date?,
  override val eventName: String,
  override val taskDefinitionKey: String,
  override val candidateUsers: List<String> = listOf(),
  override val candidateGroups: List<String> = listOf(),
  override val assignee: String?,
  override val owner: String?,
  override val dueDate: Date? = null,
  override val formKey: String?,
  override val businessKey: String?,
  override val payload: VariableMap = Variables.createVariables(),
  override val correlations: CorrelationMap = newCorrelations(),
  override var enriched: Boolean = false
) : TaskCommand


data class ChangeTaskAttributesCommand(
  @TargetAggregateIdentifier
  override val id: String,
  override val name: String?,
  override val description: String?,
  override val priority: Int?,
  override val processReference: ProcessReference?,
  override val caseReference: CaseReference?,
  override val createTime: Date?,
  override val taskDefinitionKey: String,
  override val eventName: String = "change",
  override val candidateUsers: List<String>,
  override val candidateGroups: List<String>,
  override val assignee: String?,
  override val owner: String?,
  override val dueDate: Date?,
  override val formKey: String?,
  override val businessKey: String?,
  override val payload: VariableMap = Variables.createVariables(),
  override val correlations: CorrelationMap = newCorrelations(),
  override var enriched: Boolean = false
) : TaskCommand
