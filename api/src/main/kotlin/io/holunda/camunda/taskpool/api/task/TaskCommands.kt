package io.holunda.camunda.taskpool.api.task

import org.axonframework.commandhandling.TargetAggregateIdentifier
import java.util.*

interface TaskCommand : PayloadCommand {
  val id: String
  val name: String?
  val description: String?
  val priority: Int?
  val processReference: ProcessReference?
  val caseReference: CaseReference?
  val createTime: Date?
  val taskDefinitionKey: String
  val eventName: String?
  val candidateUsers: List<String>
  val candidateGroups: List<String>
  val assignee: String?
  val owner: String?
  val dueDate: Date?
  val deleteReason: String?
}

data class ProcessReference(
  val processInstanceId: String,
  val executionId: String,
  val processDefinitionId: String
)

data class CaseReference(
  val caseInstanceId: String,
  val caseExecutionId: String,
  val caseDefinitionId: String
)

data class CreateTaskCommand(
  @TargetAggregateIdentifier
  override val id: String,
  override val name: String?,
  override val description: String?,
  override val priority: Int?,
  override val processReference: ProcessReference?,
  override val caseReference: CaseReference?,
  override val createTime: Date?,
  override val taskDefinitionKey: String,
  override val eventName: String?,
  override val candidateUsers: List<String>,
  override val candidateGroups: List<String>,
  override val assignee: String?,
  override val owner: String?,
  override val dueDate: Date?,
  override val deleteReason: String?,
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
  override val eventName: String?,
  override val candidateUsers: List<String>,
  override val candidateGroups: List<String>,
  override val assignee: String?,
  override val owner: String?,
  override val dueDate: Date?,
  override val deleteReason: String?,
  override val payload: MutableMap<String, Any> = mutableMapOf(),
  override var enriched: Boolean = false
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
  override val eventName: String?,
  override val candidateUsers: List<String>,
  override val candidateGroups: List<String>,
  override val assignee: String?,
  override val owner: String?,
  override val dueDate: Date?,
  override val deleteReason: String?,
  override val payload: MutableMap<String, Any> = mutableMapOf(),
  override var enriched: Boolean = false
) : TaskCommand

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
  override val eventName: String?,
  override val candidateUsers: List<String>,
  override val candidateGroups: List<String>,
  override val assignee: String?,
  override val owner: String?,
  override val dueDate: Date?,
  override val deleteReason: String?,
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
  override val eventName: String?,
  override val candidateUsers: List<String>,
  override val candidateGroups: List<String>,
  override val assignee: String?,
  override val owner: String?,
  override val dueDate: Date?,
  override val deleteReason: String?,
  override val payload: MutableMap<String, Any> = mutableMapOf(),
  override var enriched: Boolean = false
) : PayloadCommand, TaskCommand

