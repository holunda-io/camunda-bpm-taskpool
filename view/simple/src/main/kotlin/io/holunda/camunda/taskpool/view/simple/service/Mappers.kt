package io.holunda.camunda.taskpool.view.simple.service

import io.holunda.camunda.taskpool.api.business.AuthorizationChange
import io.holunda.camunda.taskpool.api.business.DataEntryCreatedEvent
import io.holunda.camunda.taskpool.api.business.DataEntryUpdatedEvent
import io.holunda.camunda.taskpool.api.process.instance.ProcessInstanceCancelledEvent
import io.holunda.camunda.taskpool.api.process.instance.ProcessInstanceEndedEvent
import io.holunda.camunda.taskpool.api.process.instance.ProcessInstanceStartedEvent
import io.holunda.camunda.taskpool.api.process.variable.ProcessVariableCreatedEvent
import io.holunda.camunda.taskpool.api.process.variable.ProcessVariableUpdatedEvent
import io.holunda.camunda.taskpool.view.*

/**
 * Event to entry for an update, if an optional entry exists.
 */
fun DataEntryUpdatedEvent.toDataEntry(oldEntry: DataEntry?) = if (oldEntry == null) {
  DataEntry(
    entryType = this.entryType,
    entryId = this.entryId,
    payload = this.payload,
    correlations = this.correlations,
    name = this.name,
    applicationName = this.applicationName,
    type = this.type,
    description = this.description,
    state = this.state,
    formKey = this.formKey,
    authorizedUsers = AuthorizationChange.applyUserAuthorization(listOf(), this.authorizations),
    authorizedGroups = AuthorizationChange.applyGroupAuthorization(listOf(), this.authorizations),
    protocol = addModification(listOf(), this.updateModification, this.state)
  )
} else {
  oldEntry.copy(
    payload = this.payload,
    correlations = this.correlations,
    name = this.name,
    applicationName = this.applicationName,
    type = this.type,
    description = this.description,
    state = this.state,
    formKey = this.formKey,
    authorizedUsers = AuthorizationChange.applyUserAuthorization(oldEntry.authorizedUsers, this.authorizations),
    authorizedGroups = AuthorizationChange.applyGroupAuthorization(oldEntry.authorizedGroups, this.authorizations),
    protocol = addModification(oldEntry.protocol, this.updateModification, this.state)
  )
}

/**
 * Event to entry.
 */
fun DataEntryCreatedEvent.toDataEntry() = DataEntry(
  entryType = this.entryType,
  entryId = this.entryId,
  payload = this.payload,
  correlations = this.correlations,
  name = this.name,
  applicationName = this.applicationName,
  type = this.type,
  description = this.description,
  state = this.state,
  formKey = this.formKey,
  authorizedUsers = AuthorizationChange.applyUserAuthorization(listOf(), this.authorizations),
  authorizedGroups = AuthorizationChange.applyGroupAuthorization(listOf(), this.authorizations),
  protocol = addModification(listOf(), this.createModification, this.state)
)

/**
 * Converts event to view model.
 */
fun ProcessInstanceStartedEvent.toProcessInstance(): ProcessInstance = ProcessInstance(
  processInstanceId = this.processInstanceId,
  sourceReference = this.sourceReference,
  businessKey = this.businessKey,
  superInstanceId = this.superInstanceId,
  startActivityId = this.startActivityId,
  startUserId = this.startUserId,
  state = ProcessInstanceState.RUNNING
)

/**
 * Converts event to view model.
 * @param processInstance an old version of the view model.
 */
fun ProcessInstanceEndedEvent.toProcessInstance(processInstance: ProcessInstance?): ProcessInstance = ProcessInstance(
  processInstanceId = this.processInstanceId,
  sourceReference = this.sourceReference,
  businessKey = this.businessKey,
  superInstanceId = this.superInstanceId,
  endActivityId = this.endActivityId,
  startActivityId = processInstance?.startActivityId,
  startUserId = processInstance?.startUserId,
  state = ProcessInstanceState.FINISHED
)

/**
 * Converts event to view model.
 * @param processInstance an old version of the view model.
 */
fun ProcessInstanceCancelledEvent.toProcessInstance(processInstance: ProcessInstance?): ProcessInstance = ProcessInstance(
  processInstanceId = this.processInstanceId,
  sourceReference = this.sourceReference,
  businessKey = this.businessKey,
  superInstanceId = this.superInstanceId,
  endActivityId = this.endActivityId,
  deleteReason = this.deleteReason,
  startActivityId = processInstance?.startActivityId,
  startUserId = processInstance?.startUserId,
  state = ProcessInstanceState.CANCELLED
)

/**
 * Converts event to view model.
 */
fun ProcessVariableCreatedEvent.toProcessVariable(): ProcessVariable = ProcessVariable(
  variableName = this.variableName,
  variableInstanceId = this.variableInstanceId,
  sourceReference = this.sourceReference,
  scopeActivityInstanceId = this.scopeActivityInstanceId
)

/**
 * Converts event to view model.
 */
fun ProcessVariableUpdatedEvent.toProcessVariable(processVariable: ProcessVariable? = null): ProcessVariable = ProcessVariable(
  variableName = this.variableName,
  variableInstanceId = this.variableInstanceId,
  sourceReference = this.sourceReference,
  scopeActivityInstanceId = this.scopeActivityInstanceId
)

