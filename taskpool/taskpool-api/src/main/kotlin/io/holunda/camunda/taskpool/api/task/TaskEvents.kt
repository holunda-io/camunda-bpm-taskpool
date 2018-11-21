package io.holunda.camunda.taskpool.api.task

import io.holunda.camunda.taskpool.api.business.CorrelationMap
import io.holunda.camunda.taskpool.api.business.WithCorrelations
import io.holunda.camunda.taskpool.api.business.newCorrelations
import org.axonframework.serialization.Revision
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables
import java.util.*

sealed class TaskEvent(val eventType: String) : TaskIdentity

sealed class TaskEngineEvent(eventType: String) : TaskEvent(eventType), WithPayload, WithCorrelations
sealed class TaskInteractionEvent(eventType: String) : TaskEvent(eventType)

@Revision("1")
data class TaskCreatedEngineEvent(
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
) : TaskEngineEvent("create")

@Revision("1")
data class TaskAssignedEngineEvent(
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
) : TaskEngineEvent("assign")

@Revision("1")
data class TaskCompletedEngineEvent(
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
) : TaskEngineEvent("complete")

@Revision("1")
data class TaskDeletedEngineEvent(
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
) : TaskEngineEvent("delete")


@Revision("1")
data class TaskClaimedEvent(
  override val id: String,
  override val sourceReference: SourceReference,
  override val taskDefinitionKey: String,
  val assignee: String
) : TaskInteractionEvent("claim")

@Revision("1")
data class TaskUnclaimedEvent(
  override val id: String,
  override val sourceReference: SourceReference,
  override val taskDefinitionKey: String
) : TaskInteractionEvent("unclaim")

@Revision("1")
data class TaskToBeCompletedEvent(
  override val id: String,
  override val sourceReference: SourceReference,
  override val taskDefinitionKey: String,
  val payload: VariableMap = Variables.createVariables()
) : TaskInteractionEvent("mark-complete")
