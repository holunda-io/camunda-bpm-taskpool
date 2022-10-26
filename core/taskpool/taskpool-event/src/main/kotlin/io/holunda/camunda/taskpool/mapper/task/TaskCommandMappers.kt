package io.holunda.camunda.taskpool.mapper.task

import io.holunda.camunda.taskpool.api.task.CreateTaskCommand
import io.holunda.camunda.taskpool.api.task.TaskCreatedEngineEvent
import io.holunda.camunda.taskpool.api.task.UpdateAttributeTaskCommand
import io.holunda.camunda.taskpool.model.Task

fun Task.Companion.from(command: UpdateAttributeTaskCommand): Task = Task().apply {
  id = command.id
  taskDefinitionKey = command.taskDefinitionKey
  sourceReference = command.sourceReference
  name = command.name
  description = command.description
  priority = command.priority
  owner = command.owner
  dueDate = command.dueDate
  payload = command.payload
  correlations = command.correlations
  businessKey = command.businessKey
  followUpDate = command.followUpDate
}

fun Task.Companion.from(command: CreateTaskCommand): Task = Task().apply {
  id = command.id
  taskDefinitionKey = command.taskDefinitionKey
  sourceReference = command.sourceReference
  formKey = command.formKey
  name = command.name
  description = command.description
  priority = command.priority
  owner = command.owner
  dueDate = command.dueDate
  createTime = command.createTime
  candidateUsers = command.candidateUsers
  candidateGroups = command.candidateGroups
  assignee = command.assignee
  payload = command.payload
  correlations = command.correlations
  businessKey = command.businessKey
  followUpDate = command.followUpDate
}

fun Task.Companion.from(command: TaskCreatedEngineEvent): Task = Task().apply {
  id = command.id
  taskDefinitionKey = command.taskDefinitionKey
  sourceReference = command.sourceReference
  formKey = command.formKey
  name = command.name
  description = command.description
  priority = command.priority
  owner = command.owner
  dueDate = command.dueDate
  createTime = command.createTime
  candidateUsers = command.candidateUsers
  candidateGroups = command.candidateGroups
  assignee = command.assignee
  payload = command.payload
  correlations = command.correlations
  businessKey = command.businessKey
  followUpDate = command.followUpDate
}
