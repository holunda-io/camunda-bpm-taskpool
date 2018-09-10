package io.holunda.camunda.taskpool.api.task

import java.util.*

data class TaskCreatedEvent(
  override val id: String,
  override val processReference: ProcessReference? = null,
  override val caseReference: CaseReference? = null,
  override val taskDefinitionKey: String,
  override val payload: MutableMap<String, Any> = mutableMapOf(),
  override val businessKey: String?,
  override var enriched: Boolean = true,
  val name: String? = null,
  val description: String? = null,
  val formKey: String? = null,
  val priority: Int? ,
  val createTime: Date? = null,
  val candidateUsers: List<String> = listOf(),
  val candidateGroups: List<String> = listOf(),
  val assignee: String? = null,
  val owner: String? = null,
  val dueDate: Date? = null
) : TaskIdentity, WithPayload

data class TaskAssignedEvent(
  override val id: String,
  override val processReference: ProcessReference? = null,
  override val caseReference: CaseReference? = null,
  override val taskDefinitionKey: String,
  override val payload: MutableMap<String, Any> = mutableMapOf(),
  override val businessKey: String?,
  override var enriched: Boolean = true,
  val name: String? = null,
  val description: String? = null,
  val formKey: String? = null,
  val priority: Int? ,
  val createTime: Date? = null,
  val candidateUsers: List<String> = listOf(),
  val candidateGroups: List<String> = listOf(),
  val assignee: String? = null,
  val owner: String? = null,
  val dueDate: Date? = null
) : TaskIdentity, WithPayload

data class TaskCompletedEvent(
  override val id: String,
  override val processReference: ProcessReference? = null,
  override val caseReference: CaseReference? = null,
  override val taskDefinitionKey: String,
  override val payload: MutableMap<String, Any> = mutableMapOf(),
  override val businessKey: String?,
  override var enriched: Boolean = true,
  val name: String? = null,
  val description: String? = null,
  val formKey: String? = null,
  val priority: Int? ,
  val createTime: Date? = null,
  val candidateUsers: List<String> = listOf(),
  val candidateGroups: List<String> = listOf(),
  val assignee: String? = null,
  val owner: String? = null,
  val dueDate: Date? = null
) : TaskIdentity, WithPayload

data class TaskDeletedEvent(
  override val id: String,
  override val processReference: ProcessReference? = null,
  override val caseReference: CaseReference? = null,
  override val taskDefinitionKey: String,
  override val payload: MutableMap<String, Any> = mutableMapOf(),
  override val businessKey: String?,
  override var enriched: Boolean = true,
  val name: String? = null,
  val description: String? = null,
  val formKey: String? = null,
  val priority: Int? ,
  val createTime: Date? = null,
  val candidateUsers: List<String> = listOf(),
  val candidateGroups: List<String> = listOf(),
  val assignee: String? = null,
  val owner: String? = null,
  val dueDate: Date? = null,
  val deleteReason: String?
) : TaskIdentity, WithPayload

