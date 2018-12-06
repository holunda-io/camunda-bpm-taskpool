package io.holunda.camunda.taskpool.view

import io.holunda.camunda.taskpool.api.business.CorrelationMap
import io.holunda.camunda.taskpool.api.business.WithCorrelations
import io.holunda.camunda.taskpool.api.business.newCorrelations
import io.holunda.camunda.taskpool.api.task.*
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables
import java.util.*

data class Task(
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
  val followUpDate: Date? = null
) : TaskIdentity, WithPayload, WithCorrelations

fun task(event: TaskAssignedEngineEvent) = Task(
  id = event.id,
  sourceReference = event.sourceReference,
  dueDate = event.dueDate,
  correlations = event.correlations,
  payload = event.payload,
  description = event.description,
  businessKey = event.businessKey,
  formKey = event.formKey,
  priority = event.priority,
  assignee = event.assignee,
  candidateGroups = event.candidateGroups,
  candidateUsers = event.candidateUsers,
  name = event.name,
  owner = event.owner,
  taskDefinitionKey = event.taskDefinitionKey,
  createTime = event.createTime,
  followUpDate = event.followUpDate
)

fun task(event: TaskCreatedEngineEvent) = Task(
  id = event.id,
  sourceReference = event.sourceReference,
  dueDate = event.dueDate,
  correlations = event.correlations,
  payload = event.payload,
  description = event.description,
  businessKey = event.businessKey,
  formKey = event.formKey,
  priority = event.priority,
  assignee = event.assignee,
  candidateGroups = event.candidateGroups,
  candidateUsers = event.candidateUsers,
  name = event.name,
  owner = event.owner,
  taskDefinitionKey = event.taskDefinitionKey,
  createTime = event.createTime,
  followUpDate = event.followUpDate
)

fun task(event: TaskAttributeUpdatedEngineEvent, task: Task) = Task(
  id = event.id,
  sourceReference = event.sourceReference,
  taskDefinitionKey = event.taskDefinitionKey,

  correlations = task.correlations,
  payload = task.payload,
  businessKey = task.businessKey,
  formKey = task.formKey,
  createTime = task.createTime,
  candidateGroups = task.candidateGroups,
  candidateUsers = task.candidateUsers,

  name = event.name,
  description = event.description,
  priority = event.priority,
  assignee = event.assignee,
  owner = event.owner,
  followUpDate = event.followUpDate

)
