package io.holunda.camunda.taskpool.api.task

import java.util.*

data class TaskCreatedEvent(
  val id: String,
  val name: String?,
  val description: String?,
  val priority: Int?,
  val processReference: ProcessReference?,
  val caseReference: CaseReference?,
  val createTime: Date?,
  val taskDefinitionKey: String,
  val eventName: String?,
  val candidateUsers: List<String>,
  val candidateGroups: List<String>,
  val assignee: String?,
  val owner: String?,
  val dueDate: Date?,
  val deleteReason: String?,
  val payload: MutableMap<String, Any> = mutableMapOf()
)

data class TaskAssignedEvent(
  val id: String,
  val name: String?,
  val description: String?,
  val priority: Int?,
  val processReference: ProcessReference?,
  val caseReference: CaseReference?,
  val createTime: Date?,
  val taskDefinitionKey: String,
  val eventName: String?,
  val candidateUsers: List<String>,
  val candidateGroups: List<String>,
  val assignee: String?,
  val owner: String?,
  val dueDate: Date?,
  val deleteReason: String?,
  val payload: MutableMap<String, Any> = mutableMapOf()
)

data class TaskCompletedEvent(
  val id: String,
  val name: String?,
  val description: String?,
  val priority: Int?,
  val processReference: ProcessReference?,
  val caseReference: CaseReference?,
  val createTime: Date?,
  val taskDefinitionKey: String,
  val eventName: String? = "create",
  val candidateUsers: List<String>,
  val candidateGroups: List<String>,
  val assignee: String?,
  val owner: String?,
  val dueDate: Date?,
  val deleteReason: String?,
  val payload: MutableMap<String, Any> = mutableMapOf()
) {

}

data class TaskDeletedEvent(
  val id: String,
  val name: String?,
  val description: String?,
  val priority: Int?,
  val processReference: ProcessReference?,
  val caseReference: CaseReference?,
  val createTime: Date?,
  val taskDefinitionKey: String,
  val eventName: String?,
  val candidateUsers: List<String>,
  val candidateGroups: List<String>,
  val assignee: String?,
  val owner: String?,
  val dueDate: Date?,
  val deleteReason: String?,
  val payload: MutableMap<String, Any> = mutableMapOf()
)

