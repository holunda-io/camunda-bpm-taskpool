package io.holunda.camunda.taskpool.api.task

import io.holunda.camunda.taskpool.api.business.CorrelationMap
import io.holunda.camunda.taskpool.api.business.WithCorrelations
import io.holunda.camunda.taskpool.api.business.newCorrelations
import org.axonframework.serialization.Revision
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables
import java.util.*

sealed class TaskEvent : TaskIdentity, WithPayload, WithCorrelations

@Revision("1")
data class TaskCreatedEvent(
  override val id: String,
  override val sourceReference: SourceReference,
  override val taskDefinitionKey: String,
  override val payload: VariableMap = Variables.createVariables(),
  override val correlations: CorrelationMap = newCorrelations(),
  override val businessKey: String? = null,
  override var enriched: Boolean = true,
  val name: String? = null,
  val description: String? = null,
  val formKey: String? = null,
  val priority: Int? = 0,
  val createTime: Date? = null,
  val candidateUsers: List<String> = listOf(),
  val candidateGroups: List<String> = listOf(),
  val assignee: String? = null,
  val owner: String? = null,
  val dueDate: Date? = null
) : TaskEvent()

@Revision("1")
data class TaskAssignedEvent(
  override val id: String,
  override val sourceReference: SourceReference,
  override val taskDefinitionKey: String,
  override val payload: VariableMap = Variables.createVariables(),
  override val correlations: CorrelationMap = newCorrelations(),
  override val businessKey: String? = null,
  override var enriched: Boolean = true,
  val name: String? = null,
  val description: String? = null,
  val formKey: String? = null,
  val priority: Int? = 0,
  val createTime: Date? = null,
  val candidateUsers: List<String> = listOf(),
  val candidateGroups: List<String> = listOf(),
  val assignee: String? = null,
  val owner: String? = null,
  val dueDate: Date? = null
) : TaskEvent()

@Revision("1")
data class TaskCompletedEvent(
  override val id: String,
  override val sourceReference: SourceReference,
  override val taskDefinitionKey: String,

  override val payload: VariableMap = Variables.createVariables(),
  override val correlations: CorrelationMap = newCorrelations(),
  override val businessKey: String? = null,
  override var enriched: Boolean = true,
  val name: String? = null,
  val description: String? = null,
  val formKey: String? = null,
  val priority: Int? = 0,
  val createTime: Date? = null,
  val candidateUsers: List<String> = listOf(),
  val candidateGroups: List<String> = listOf(),
  val assignee: String? = null,
  val owner: String? = null,
  val dueDate: Date? = null
) : TaskEvent()

@Revision("1")
data class TaskDeletedEvent(
  override val id: String,
  override val sourceReference: SourceReference,
  override val taskDefinitionKey: String,

  override val payload: VariableMap = Variables.createVariables(),
  override val correlations: CorrelationMap = newCorrelations(),
  override val businessKey: String? = null,
  override var enriched: Boolean = true,
  val name: String? = null,
  val description: String? = null,
  val formKey: String? = null,
  val priority: Int? = 0,
  val createTime: Date? = null,
  val candidateUsers: List<String> = listOf(),
  val candidateGroups: List<String> = listOf(),
  val assignee: String? = null,
  val owner: String? = null,
  val dueDate: Date? = null,
  val deleteReason: String?
) : TaskEvent()

@Revision("1")
data class TaskClaimedEvent(
  override val id: String,
  override val sourceReference: SourceReference,
  override val taskDefinitionKey: String,
  val assignee: String
) :TaskIdentity

@Revision("1")
data class TaskUnclaimedEvent(
  override val id: String,
  override val sourceReference: SourceReference,
  override val taskDefinitionKey: String
) : TaskIdentity

@Revision("1")
data class TaskToBeCompletedEvent(
  override val id: String,
  override val sourceReference: SourceReference,
  override val taskDefinitionKey: String,
  val payload: VariableMap = Variables.createVariables()
) : TaskIdentity
