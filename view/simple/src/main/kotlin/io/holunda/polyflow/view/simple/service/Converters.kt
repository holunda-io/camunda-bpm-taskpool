package io.holunda.polyflow.view.simple.service

import io.holunda.camunda.taskpool.api.business.*
import io.holunda.camunda.taskpool.api.process.instance.ProcessInstanceCancelledEvent
import io.holunda.camunda.taskpool.api.process.instance.ProcessInstanceEndedEvent
import io.holunda.camunda.taskpool.api.process.instance.ProcessInstanceStartedEvent
import io.holunda.camunda.taskpool.api.process.variable.ProcessVariableCreate
import io.holunda.camunda.taskpool.api.process.variable.ProcessVariableUpdate
import io.holunda.camunda.taskpool.api.process.variable.ProcessVariablesChangedEvent
import io.holunda.camunda.taskpool.api.task.SourceReference
import io.holunda.polyflow.view.*

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
    authorizedUsers = AuthorizationChange.applyUserAuthorization(setOf(), this.authorizations),
    authorizedGroups = AuthorizationChange.applyGroupAuthorization(setOf(), this.authorizations),
    protocol = listOf<ProtocolEntry>().addModification(this.updateModification, this.state)
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
    protocol = oldEntry.protocol.addModification(this.updateModification, this.state)
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
  authorizedUsers = AuthorizationChange.applyUserAuthorization(setOf(), this.authorizations),
  authorizedGroups = AuthorizationChange.applyGroupAuthorization(setOf(), this.authorizations),
  protocol = listOf<ProtocolEntry>().addModification(this.createModification, this.state)
)

/**
 * Event to entry for an update, if an optional entry exists.
 */
fun DataEntryAnonymizedEvent.toDataEntry(oldEntry: DataEntry) =
  oldEntry.copy(
    authorizedUsers = AuthorizationChange.applyUserAuthorization(oldEntry.authorizedUsers,
      oldEntry.authorizedUsers.map { AuthorizationChange.removeUser(it) }),
    protocol = oldEntry.protocol.map {
      it.copy(
        username =
        if (it.username != null && !this.excludedUsernames.contains(it.username))
          this.anonymizedUsername else it.username) }
      .addModification(this.anonymizeModification, oldEntry.state))

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
 * Converts event to view model, only used if the real process instance is missing.
 */
fun ProcessVariablesChangedEvent.toProcessInstance(): ProcessInstance = ProcessInstance(
  processInstanceId = this.sourceReference.instanceId,
  sourceReference = this.sourceReference,
  state = ProcessInstanceState.RUNNING
)

/**
 * Converts a process variable change into process variable.
 * @param source reference of the change
 * @return process variable
 */
fun ProcessVariableCreate.toProcessVariable(source: SourceReference) = ProcessVariable(
  variableName = this.variableName,
  variableInstanceId = this.variableInstanceId,
  sourceReference = source,
  scopeActivityInstanceId = this.scopeActivityInstanceId,
  value = this.value
)

/**
 * Converts a process variable change into process variable.
 * @param source reference of the change
 * @return process variable
 */
fun ProcessVariableUpdate.toProcessVariable(source: SourceReference) = ProcessVariable(
  variableName = this.variableName,
  variableInstanceId = this.variableInstanceId,
  sourceReference = source,
  scopeActivityInstanceId = this.scopeActivityInstanceId,
  value = this.value
)
