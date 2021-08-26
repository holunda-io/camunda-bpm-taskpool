package io.holunda.polyflow.view.jpa.process

import io.holunda.camunda.taskpool.api.process.definition.ProcessDefinitionRegisteredEvent
import io.holunda.camunda.taskpool.api.process.instance.ProcessInstanceCancelledEvent
import io.holunda.camunda.taskpool.api.process.instance.ProcessInstanceEndedEvent
import io.holunda.camunda.taskpool.api.process.instance.ProcessInstanceStartedEvent
import io.holunda.camunda.taskpool.api.task.CaseReference
import io.holunda.camunda.taskpool.api.task.GenericReference
import io.holunda.camunda.taskpool.api.task.ProcessReference
import io.holunda.camunda.taskpool.api.task.SourceReference
import io.holunda.polyflow.view.ProcessDefinition
import io.holunda.polyflow.view.ProcessInstance
import io.holunda.polyflow.view.ProcessInstanceState
import io.holunda.polyflow.view.jpa.auth.AuthorizationPrincipal
import io.holunda.polyflow.view.jpa.auth.AuthorizationPrincipal.Companion.group
import io.holunda.polyflow.view.jpa.auth.AuthorizationPrincipal.Companion.user
import io.holunda.polyflow.view.jpa.auth.AuthorizationPrincipalType.GROUP
import io.holunda.polyflow.view.jpa.auth.AuthorizationPrincipalType.USER

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
fun SourceReference.toSourceReferenceEmbeddable() = SourceReferenceEmbeddable(
  instanceId = this.instanceId,
  executionId = this.executionId,
  definitionId = this.definitionId,
  definitionKey = this.definitionKey,
  name = this.name,
  applicationName = this.applicationName,
  tenantId = this.tenantId,
  sourceType = when (this) {
    is ProcessReference -> "PROCESS"
    is CaseReference -> "CASE"
    else -> "GENERIC"
  }
)

/**
 * Converts embeddable into View API DTO.
 */
fun SourceReferenceEmbeddable.toSourceReference() = when (this.sourceType) {
  "CASE" -> CaseReference(
    instanceId = this.instanceId,
    executionId = this.executionId,
    definitionId = this.definitionId,
    definitionKey = this.definitionKey,
    name = this.name,
    applicationName = this.applicationName,
    tenantId = this.tenantId
  )
  "PROCESS" -> ProcessReference(
    instanceId = this.instanceId,
    executionId = this.executionId,
    definitionId = this.definitionId,
    definitionKey = this.definitionKey,
    name = this.name,
    applicationName = this.applicationName,
    tenantId = this.tenantId
  )
  "GENERIC" -> GenericReference(
    instanceId = this.instanceId,
    executionId = this.executionId,
    definitionId = this.definitionId,
    definitionKey = this.definitionKey,
    name = this.name,
    applicationName = this.applicationName,
    tenantId = this.tenantId
  )
  else -> throw IllegalArgumentException("Unexpected source reference of type ${this.sourceType}")
}
