package io.holunda.camunda.taskpool.mapper.task

import io.holunda.camunda.taskpool.api.task.CreateTaskCommand
import io.holunda.camunda.taskpool.api.task.TaskAttributeUpdatedEngineEvent
import io.holunda.camunda.taskpool.api.task.TaskCreatedEngineEvent
import io.holunda.camunda.taskpool.api.task.UpdateAttributeTaskCommand
import io.holunda.camunda.taskpool.model.Task

/**
 * Constructs a task representation from create command.
 */
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

/**
 * Constructs a task representation from update command.
 */
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

/**
 * Constructs a task representation from created event.
 */
fun Task.Companion.from(event: TaskCreatedEngineEvent): Task = Task().apply {
  id = event.id
  taskDefinitionKey = event.taskDefinitionKey
  sourceReference = event.sourceReference
  formKey = event.formKey
  name = event.name
  description = event.description
  priority = event.priority
  owner = event.owner
  dueDate = event.dueDate
  createTime = event.createTime
  candidateUsers = event.candidateUsers
  candidateGroups = event.candidateGroups
  assignee = event.assignee
  payload = event.payload
  correlations = event.correlations
  businessKey = event.businessKey
  followUpDate = event.followUpDate
}

/**
 * Constructs a task representation from updated event.
 */
fun Task.Companion.from(event: TaskAttributeUpdatedEngineEvent): Task = Task().apply {
  id = event.id
  taskDefinitionKey = event.taskDefinitionKey
  sourceReference = event.sourceReference
  formKey = event.formKey
  name = event.name
  description = event.description
  priority = event.priority
  owner = event.owner
  dueDate = event.dueDate
  payload = event.payload
  correlations = event.correlations
  businessKey = event.businessKey
  followUpDate = event.followUpDate
}
