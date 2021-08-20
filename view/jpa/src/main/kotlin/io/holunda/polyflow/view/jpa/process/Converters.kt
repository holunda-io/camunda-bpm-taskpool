package io.holunda.polyflow.view.jpa.process

import io.holunda.camunda.taskpool.api.process.definition.ProcessDefinitionRegisteredEvent
import io.holunda.camunda.taskpool.api.process.instance.*
import io.holunda.camunda.taskpool.api.task.ProcessReference
import io.holunda.camunda.taskpool.api.task.SourceReference
import io.holunda.polyflow.view.ProcessDefinition
import io.holunda.polyflow.view.ProcessInstance
import io.holunda.polyflow.view.ProcessInstanceState
import io.holunda.polyflow.view.jpa.data.AuthorizationPrincipal
import io.holunda.polyflow.view.jpa.data.AuthorizationPrincipal.Companion.group
import io.holunda.polyflow.view.jpa.data.AuthorizationPrincipal.Companion.user
import io.holunda.polyflow.view.jpa.data.AuthorizationPrincipalType.GROUP
import io.holunda.polyflow.view.jpa.data.AuthorizationPrincipalType.USER

/**
 * Creates a new process definition from registration event.
 */
fun ProcessDefinitionRegisteredEvent.toEntity() = ProcessDefinitionEntity(
  processDefinitionId = this.processDefinitionId,
  processDefinitionKey = this.processDefinitionKey,
  processDefinitionVersion = this.processDefinitionVersion,
  applicationName = this.applicationName,
  name = this.processName,
  description = this.processDescription,
  versionTag = this.processVersionTag,
  startFormKey = this.formKey,
  startableFromTasklist = this.startableFromTasklist,
  authorizedStarterPrincipals =
  this.candidateStarterGroups.map { name -> group(name) }
    .plus(this.candidateStarterUsers.map { name -> user(name) })
    .map { it.toString() }
    .toMutableSet()
)

/**
 * Converts the entity into View API DTO.
 */
fun ProcessDefinitionEntity.toProcessDefinition() = ProcessDefinition(
  processDefinitionId = this.processDefinitionId,
  processDefinitionKey = this.processDefinitionKey,
  processDefinitionVersion = this.processDefinitionVersion,
  applicationName = this.applicationName,
  processName = this.name,
  processDescription = this.description,
  processVersionTag = this.versionTag,
  formKey = this.startFormKey,
  startableFromTasklist = this.startableFromTasklist,
  candidateStarterGroups = this.authorizedStarterPrincipals.map { AuthorizationPrincipal(it) }.filter { it.type == GROUP }.map { it.name }.toSet(),
  candidateStarterUsers = this.authorizedStarterPrincipals.map { AuthorizationPrincipal(it) }.filter { it.type == USER }.map { it.name }.toSet()
)

/**
 * Converts the entity into View API DTO.
 */
fun ProcessInstanceEntity.toProcessInstance() = ProcessInstance(
  processInstanceId = this.processInstanceId,
  sourceReference = this.sourceReference.toSourceReference(),
  state = this.state,
  businessKey = this.businessKey,
  superInstanceId = this.superInstanceId,
  startActivityId = this.startActivityId,
  endActivityId = this.endActivityId,
  startUserId = this.startUserId,
  deleteReason = this.deleteReason
)

/**
 * Converts embeddable into View API DTO.
 */
fun ProcessSourceReferenceEmbeddable.toSourceReference() = ProcessReference(
  instanceId = this.instanceId,
  executionId = this.executionId,
  definitionId = this.definitionId,
  definitionKey = this.definitionKey,
  name = this.name,
  applicationName = this.applicationName,
  tenantId = this.tenantId
)

/**
 * Creates a new entity.
 */
fun ProcessInstanceStartedEvent.toEntity() = ProcessInstanceEntity(
  processInstanceId = this.processInstanceId,
  sourceReference = this.sourceReference.toSourceReferenceEmbeddable(),
  state = ProcessInstanceState.RUNNING,
  businessKey = this.businessKey,
  superInstanceId = this.superInstanceId,
  startActivityId = this.startActivityId,
  startUserId = this.startUserId,
  endActivityId = null,
  deleteReason = null
)

/**
 * Ends running instance.
 */
fun ProcessInstanceEntity.finishInstance(event: ProcessInstanceEndedEvent) = this.apply {
  this.state = ProcessInstanceState.FINISHED
  this.endActivityId = event.endActivityId
}

/**
 * Cancels running instance.
 */
fun ProcessInstanceEntity.cancelInstance(event: ProcessInstanceCancelledEvent) = this.apply {
  this.state = ProcessInstanceState.CANCELLED
  this.endActivityId = event.endActivityId
  this.deleteReason = event.deleteReason
}

/**
 * Suspends running instance.
 */
fun ProcessInstanceEntity.suspendInstance() = this.apply {
  this.state = ProcessInstanceState.SUSPENDED
}

/**
 * Resume running instance.
 */
fun ProcessInstanceEntity.resumeInstance() = this.apply {
  this.state = ProcessInstanceState.RUNNING
}


/**
 * Converts View API DTO into embeddable.
 */
fun SourceReference.toSourceReferenceEmbeddable() = ProcessSourceReferenceEmbeddable(
  instanceId = this.instanceId,
  executionId = this.executionId,
  definitionId = this.definitionId,
  definitionKey = this.definitionKey,
  name = this.name,
  applicationName = this.applicationName,
  tenantId = this.tenantId
)
