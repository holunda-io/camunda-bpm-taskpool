package io.holunda.polyflow.taskpool.sender

import io.holunda.camunda.taskpool.api.task.CamundaTaskEventType
import io.holunda.camunda.taskpool.api.task.CreateTaskCommand
import io.holunda.camunda.taskpool.api.task.ProcessReference
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables.createVariables
import java.time.Instant
import java.util.*

class TestObjectFactory {

  companion object {
    const val PROCESS_ID = "io.holunda.process.id"
    const val PROCESS_NAME = "My PROCESS"
    const val APPLICATION_NAME = "my-app"
    const val TASK_DEFINITION_KEY = "user-task-1"
  }

  val processDefinitionId = UUID.randomUUID().toString()
  val taskId = UUID.randomUUID().toString()
  val processInstanceId = UUID.randomUUID().toString()
  val executionId = UUID.randomUUID().toString()
  val createTime = Date.from(Instant.now())


  fun createCommand(
    name: String?,
    description: String? = null,
    taskDefinitionKey: String = TASK_DEFINITION_KEY,
    candidateUsers: Set<String> = setOf(),
    candidateGroups: Set<String> = setOf(),
    assignee: String? = null,
    createTime: Date? = this.createTime,
    processBusinessKey: String? = null,
    priority: Int? = 50,
    variables: VariableMap = createVariables(),
    formKey: String? = null
  ) = CreateTaskCommand(
    id = taskId,
    sourceReference = ProcessReference(
      instanceId = processInstanceId,
      executionId = executionId,
      definitionId = processDefinitionId,
      name = PROCESS_NAME,
      definitionKey = PROCESS_ID,
      applicationName = APPLICATION_NAME
    ),
    name = name,
    description = description,
    taskDefinitionKey = taskDefinitionKey,
    candidateUsers = candidateUsers,
    candidateGroups = candidateGroups,
    assignee = assignee,
    enriched = true,
    eventName = CamundaTaskEventType.CREATE,
    createTime = createTime,
    businessKey = processBusinessKey,
    priority = priority,
    payload = variables,
    formKey = formKey
  )


}