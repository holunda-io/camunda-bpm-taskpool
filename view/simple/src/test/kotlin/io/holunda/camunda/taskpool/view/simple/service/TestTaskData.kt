package io.holunda.camunda.taskpool.view.simple.service

import io.holunda.camunda.taskpool.api.business.CorrelationMap
import io.holunda.camunda.taskpool.api.task.*
import io.holunda.camunda.taskpool.view.Task
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables
import java.util.*

data class TestTaskData(
  val id: String,
  val sourceReference: SourceReference = ProcessReference(
    "instance-id-12345",
    "execution-id-12345",
    "definition-id-12345",
    "definition-key-abcde",
    "process-name",
    "application-name"
  ),
  val taskDefinitionKey: String = "task-definition-key-abcde",
  val payload: VariableMap = Variables.fromMap(mapOf(Pair("variableKey", "variableValue"))),
  val correlations: CorrelationMap = Variables.fromMap(mapOf(Pair("correlationKey", "correlationValue"))),
  val businessKey: String? = "businessKey",
  val name: String? = "task-name",
  val description: String? = "some task description",
  val formKey: String? = "app:form-key",
  val priority: Int? = 0,
  val createTime: Date? = Date(1234567890L),
  val candidateUsers: Set<String> = setOf("kermit", "piggy"),
  val candidateGroups: Set<String> = setOf("muppetshow"),
  val assignee: String? = null,
  val owner: String? = null,
  val dueDate: Date? = Date(1234599999L),
  val followUpDate: Date? = null) {

  fun asTaskCreatedEngineEvent() = TaskCreatedEngineEvent(
    id = id,
    sourceReference = sourceReference,
    taskDefinitionKey = taskDefinitionKey,
    payload = payload,
    correlations = correlations,
    businessKey = businessKey,
    name = name,
    description = description,
    formKey = formKey,
    priority = priority,
    createTime = createTime,
    candidateUsers = candidateUsers,
    candidateGroups = candidateGroups,
    assignee = assignee,
    owner = owner,
    dueDate = dueDate,
    followUpDate = followUpDate
  )

  fun asTaskAssignedEngineEvent() = TaskAssignedEngineEvent(
    id = id,
    sourceReference = sourceReference,
    taskDefinitionKey = taskDefinitionKey,
    payload = payload,
    correlations = correlations,
    assignee = assignee)

  fun asTaskAttributeUpdatedEvent() = TaskAttributeUpdatedEngineEvent(
    id = id,
    sourceReference = sourceReference,
    taskDefinitionKey = taskDefinitionKey,
    name = name,
    description = description,
    priority = priority,
    owner = owner,
    dueDate = dueDate,
    followUpDate = followUpDate
  )

  fun asCandidateGroupChangedEvent(groupId: String, assignmentUpdateType: String) = TaskCandidateGroupChanged(
    id = id,
    sourceReference = sourceReference,
    taskDefinitionKey = taskDefinitionKey,
    groupId = groupId,
    assignmentUpdateType = assignmentUpdateType
  )

  fun asCandidateUserChangedEvent(userId: String, assignmentUpdateType: String) = TaskCandidateUserChanged(
    id = id,
    sourceReference = sourceReference,
    taskDefinitionKey = taskDefinitionKey,
    userId = userId,
    assignmentUpdateType = assignmentUpdateType
  )

  fun asTask() = Task(
    id = id,
    sourceReference = sourceReference,
    taskDefinitionKey = taskDefinitionKey,
    payload = payload,
    correlations = correlations,
    businessKey = businessKey,
    name = name,
    description = description,
    formKey = formKey,
    priority = priority,
    createTime = createTime,
    candidateUsers = candidateUsers,
    candidateGroups = candidateGroups,
    assignee = assignee,
    owner = owner,
    dueDate = dueDate,
    followUpDate = followUpDate
  )
}
