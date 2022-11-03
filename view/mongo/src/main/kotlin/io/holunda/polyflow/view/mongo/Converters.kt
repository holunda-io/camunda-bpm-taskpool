package io.holunda.polyflow.view.mongo

import io.holunda.camunda.taskpool.api.business.*
import io.holunda.polyflow.view.ProtocolEntry
import io.holunda.polyflow.view.Task
import io.holunda.polyflow.view.TaskWithDataEntries
import io.holunda.polyflow.view.addModification
import io.holunda.polyflow.view.mongo.data.DataEntryDocument
import io.holunda.polyflow.view.mongo.data.dataEntry
import io.holunda.polyflow.view.mongo.data.toProtocolElement
import io.holunda.polyflow.view.mongo.task.TaskWithDataEntriesDocument
import io.holunda.polyflow.view.mongo.task.sourceReference
import org.camunda.bpm.engine.variable.Variables


/**
 * Creates the document.
 */
fun DataEntryCreatedEvent.toDocument() = DataEntryDocument(
  identity = dataIdentityString(entryType = this.entryType, entryId = this.entryId),
  entryType = this.entryType,
  payload = this.payload,
  correlations = this.correlations,
  name = this.name,
  description = this.description,
  type = this.type,
  authorizedUsers = AuthorizationChange.applyUserAuthorization(setOf(), this.authorizations),
  authorizedGroups = AuthorizationChange.applyGroupAuthorization(setOf(), this.authorizations),
  protocol = listOf<ProtocolEntry>().addModification(this.createModification, this.state).map { it.toProtocolElement() },
  createdDate = this.createModification.time.toInstant(),
  lastModifiedDate = this.createModification.time.toInstant(),
  applicationName = this.applicationName,
  state = this.state.state,
  statusType = this.state.processingType.name
)

/**
 * Updated event to document.
 */
fun DataEntryUpdatedEvent.toDocument(oldDocument: DataEntryDocument?) = if (oldDocument != null) {
  val authorizedUsers = AuthorizationChange.applyUserAuthorization(oldDocument.getAuthorizedUsers(), this.authorizations)
  val authorizedGroups = AuthorizationChange.applyGroupAuthorization(oldDocument.getAuthorizedGroups(), this.authorizations)
  oldDocument.copy(
    entryType = this.entryType,
    payload = this.payload,
    correlations = this.correlations,
    name = this.name,
    description = this.description,
    type = this.type,
    authorizedUsers = authorizedUsers,
    authorizedGroups = authorizedGroups,
    authorizedPrincipals = DataEntryDocument.authorizedPrincipals(authorizedUsers, authorizedGroups),
    protocol = oldDocument.protocol.map { it.toProtocol() }.addModification(this.updateModification, this.state).map { it.toProtocolElement() },
    lastModifiedDate = this.updateModification.time.toInstant(),
    applicationName = this.applicationName,
    state = this.state.state,
    statusType = this.state.processingType.name
  )
} else {
  // getting an update without any element in the database.
  DataEntryDocument(
    identity = dataIdentityString(entryType = this.entryType, entryId = this.entryId),
    entryType = this.entryType,
    payload = this.payload,
    correlations = this.correlations,
    name = this.name,
    description = this.description,
    type = this.type,
    authorizedUsers = AuthorizationChange.applyUserAuthorization(setOf(), this.authorizations),
    authorizedGroups = AuthorizationChange.applyGroupAuthorization(setOf(), this.authorizations),
    protocol = listOf<ProtocolEntry>().addModification(this.updateModification, this.state).map { it.toProtocolElement() },
    createdDate = this.updateModification.time.toInstant(),
    lastModifiedDate = this.updateModification.time.toInstant(),
    applicationName = this.applicationName,
    state = this.state.state,
    statusType = this.state.processingType.name
  )
}


/**
 * Create a task with data entries from the corresponding mongo document.
 */
fun TaskWithDataEntriesDocument.taskWithDataEntries() = TaskWithDataEntries(
  task = Task(
    id = this.id,
    sourceReference = sourceReference(this.sourceReference),
    taskDefinitionKey = this.taskDefinitionKey,
    payload = Variables.fromMap(this.payload),
    businessKey = this.businessKey,
    name = this.name,
    description = this.description,
    formKey = this.formKey,
    priority = this.priority,
    createTime = this.createTime,
    candidateUsers = this.candidateUsers,
    candidateGroups = this.candidateGroups,
    assignee = this.assignee,
    owner = this.owner,
    dueDate = this.dueDate,
    followUpDate = this.followUpDate
  ),
  dataEntries = this.dataEntries.map { it.dataEntry() }
)


/**
 * Deleted event to document.
 */
fun DataEntryDeletedEvent.toDocument(oldDocument: DataEntryDocument): DataEntryDocument =
  oldDocument.copy(
    deletedDate = this.deleteModification.time.toInstant(),
    protocol = oldDocument.protocol.map { it.toProtocol() }.addModification(this.deleteModification, this.state).map { it.toProtocolElement() },
    state = this.state.state,
    statusType = this.state.processingType.name
  )

