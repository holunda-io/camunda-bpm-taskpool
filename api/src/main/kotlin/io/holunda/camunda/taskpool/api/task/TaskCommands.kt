package io.holunda.camunda.taskpool.api.task

import org.axonframework.commandhandling.TargetAggregateIdentifier
import java.util.*

interface TaskCommand : WithPayload, TaskIdentity, CamundaTaskEvent {
  val name: String?
  val description: String?
  val formKey: String?
  val createTime: Date?
  val owner: String?
  val assignee: String?
  val candidateUsers: List<String>
  val candidateGroups: List<String>
  val dueDate: Date?
  val priority: Int?
}

data class ProcessReference(
  val processInstanceId: String,
  val executionId: String,
  val processDefinitionId: String,
  val processDefinitionKey: String
)

data class CaseReference(
  val caseInstanceId: String,
  val caseExecutionId: String,
  val caseDefinitionId: String,
  val caseDefinitionKey: String
)

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
  override val payload: MutableMap<String, Any> = mutableMapOf(),
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
  override val payload: MutableMap<String, Any> = mutableMapOf(),
  override var enriched: Boolean = false
) : TaskCommand

data class DeleteTaskCommand(
  @TargetAggregateIdentifier
  override val id: String,
  override val name: String?,
  override val description: String?,
  override val priority: Int?,
  override val processReference: ProcessReference?,
  override val caseReference: CaseReference?,
  override val createTime: Date?,
  override val taskDefinitionKey: String,
  override val eventName: String = "delete",
  override val candidateUsers: List<String>,
  override val candidateGroups: List<String>,
  override val assignee: String?,
  override val owner: String?,
  override val dueDate: Date?,
  override val formKey: String?,
  override val businessKey: String?,
  override val payload: MutableMap<String, Any> = mutableMapOf(),
  override var enriched: Boolean = false,
  val deleteReason: String?
) : TaskCommand

data class CompleteTaskCommand(
  @TargetAggregateIdentifier
  override val id: String,
  override val name: String?,
  override val description: String?,
  override val priority: Int?,
  override val processReference: ProcessReference?,
  override val caseReference: CaseReference?,
  override val createTime: Date?,
  override val taskDefinitionKey: String,
  override val eventName: String = "complete",
  override val candidateUsers: List<String>,
  override val candidateGroups: List<String>,
  override val assignee: String?,
  override val owner: String?,
  override val dueDate: Date?,
  override val formKey: String?,
  override val businessKey: String?,
  override val payload: MutableMap<String, Any> = mutableMapOf(),
  override var enriched: Boolean = false
) : TaskCommand

data class CreateOrAssignTaskCommand(
  @TargetAggregateIdentifier
  override val id: String,
  override val name: String?,
  override val description: String?,
  override val priority: Int?,
  override val processReference: ProcessReference?,
  override val caseReference: CaseReference?,
  override val createTime: Date?,
  override val eventName: String,
  override val taskDefinitionKey: String,
  override val candidateUsers: List<String>,
  override val candidateGroups: List<String>,
  override val assignee: String?,
  override val owner: String?,
  override val dueDate: Date?,
  override val formKey: String?,
  override val businessKey: String?,
  override val payload: MutableMap<String, Any> = mutableMapOf(),
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
  override val payload: MutableMap<String, Any> = mutableMapOf(),
  override var enriched: Boolean = false
) : TaskCommand
