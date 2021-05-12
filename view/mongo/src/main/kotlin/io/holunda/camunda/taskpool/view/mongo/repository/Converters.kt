package io.holunda.camunda.taskpool.view.mongo.repository

import io.holunda.camunda.taskpool.api.business.*
import io.holunda.camunda.taskpool.api.task.CaseReference
import io.holunda.camunda.taskpool.api.task.GenericReference
import io.holunda.camunda.taskpool.api.task.ProcessReference
import io.holunda.camunda.taskpool.api.task.SourceReference
import io.holunda.camunda.taskpool.view.ProtocolEntry
import io.holunda.camunda.taskpool.view.Task
import io.holunda.camunda.taskpool.view.TaskWithDataEntries
import io.holunda.camunda.taskpool.view.addModification
import org.camunda.bpm.engine.variable.Variables

/**
 * Create a task document from task.
 */
fun Task.taskDocument() = TaskDocument(
  id = this.id,
  sourceReference = when (val reference = this.sourceReference) {
    is ProcessReference -> ProcessReferenceDocument(reference)
    is CaseReference -> CaseReferenceDocument(reference)
    is GenericReference -> GenericReferenceDocument(reference)
    else -> throw IllegalArgumentException("Unknown source reference of ${reference::class.java}")
  },
  taskDefinitionKey = this.taskDefinitionKey,
  payload = this.payload.toMutableMap(),
  // FIXME: maybe remove?
  correlations = this.correlations.toMutableMap(),
  dataEntriesRefs = this.correlations.map { dataIdentityString(entryType = it.key, entryId = it.value as EntryId) }.toSet(),
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
)

/**
 * Create a task from task document.
 */
fun TaskDocument.task() = Task(
  id = this.id,
  sourceReference = sourceReference(this.sourceReference),
  taskDefinitionKey = this.taskDefinitionKey,
  payload = Variables.fromMap(this.payload),
  correlations = Variables.fromMap(this.correlations),
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
  followUpDate = this.followUpDate,
  deleted = this.deleted
)

/**
 * Create source reference out of reference document.
 */
fun sourceReference(reference: ReferenceDocument): SourceReference =
  when (reference) {
    is ProcessReferenceDocument -> ProcessReference(
      instanceId = reference.instanceId,
      executionId = reference.executionId,
      definitionId = reference.definitionId,
      definitionKey = reference.definitionKey,
      name = reference.name,
      applicationName = reference.applicationName,
      tenantId = reference.tenantId
    )
    is CaseReferenceDocument -> CaseReference(
      instanceId = reference.instanceId,
      executionId = reference.executionId,
      definitionId = reference.definitionId,
      definitionKey = reference.definitionKey,
      name = reference.name,
      applicationName = reference.applicationName,
      tenantId = reference.tenantId
    )
    is GenericReferenceDocument -> GenericReference(
      instanceId = reference.instanceId,
      executionId = reference.executionId,
      definitionId = reference.definitionId,
      definitionKey = reference.definitionKey,
      name = reference.name,
      applicationName = reference.applicationName,
      tenantId = reference.tenantId
    )

  }

/**
 * Constructs a data entry out of Data Entry Document.
 */
fun DataEntryDocument.dataEntry() =
  if (identity.contains(DATA_IDENTITY_SEPARATOR)) {
    with(identity.split(DATA_IDENTITY_SEPARATOR)) {
      io.holunda.camunda.taskpool.view.DataEntry(
        entryType = this[0],
        entryId = this[1],
        payload = Variables.fromMap(payload),
        correlations = Variables.fromMap(correlations),
        name = name,
        description = description,
        type = type,
        authorizedUsers = authorizedUsers,
        authorizedGroups = authorizedGroups,
        applicationName = applicationName,
        state = mapState(state, statusType),
        protocol = protocol.map { it.toProtocol() }
      )
    }
  } else {
    throw IllegalArgumentException("Identity could not be split into entry type and id, because it doesn't contain the '$DATA_IDENTITY_SEPARATOR'. Value was $identity")
  }

/**
 * Maps the state.
 */
fun mapState(state: String?, statusType: String?): DataEntryState = if (state != null) {
  when (statusType) {
    "PRELIMINARY" -> ProcessingType.PRELIMINARY.of(state)
    "IN_PROGRESS" -> ProcessingType.IN_PROGRESS.of(state)
    "COMPLETED" -> ProcessingType.COMPLETED.of(state)
    "CANCELLED" -> ProcessingType.CANCELLED.of(state)
    else -> ProcessingType.UNDEFINED.of(state)
  }
} else {
  ProcessingType.UNDEFINED.of("")
}

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
  authorizedUsers = AuthorizationChange.applyUserAuthorization(listOf(), this.authorizations),
  authorizedGroups = AuthorizationChange.applyGroupAuthorization(listOf(), this.authorizations),
  protocol = addModification(listOf(), this.createModification, this.state).map { it.toProtocolElement()},
  applicationName = this.applicationName,
  state = this.state.state,
  statusType = this.state.processingType.name
)

/**
 * Creates the document.
 */
fun DataEntryUpdatedEvent.toDocument(oldDocument: DataEntryDocument?) = if (oldDocument != null) {
  oldDocument.copy(
    entryType = this.entryType,
    payload = this.payload,
    correlations = this.correlations,
    name = this.name,
    description = this.description,
    type = this.type,
    authorizedUsers = AuthorizationChange.applyUserAuthorization(oldDocument.authorizedUsers, this.authorizations),
    authorizedGroups = AuthorizationChange.applyGroupAuthorization(oldDocument.authorizedGroups, this.authorizations),
    protocol = addModification(oldDocument.protocol.map{ it.toProtocol() }, this.updateModification, this.state).map { it.toProtocolElement()},
    applicationName = this.applicationName,
    state = this.state.state,
    statusType = this.state.processingType.name
  )
} else {
  DataEntryDocument(
    identity = dataIdentityString(entryType = this.entryType, entryId = this.entryId),
    entryType = this.entryType,
    payload = this.payload,
    correlations = this.correlations,
    name = this.name,
    description = this.description,
    type = this.type,
    authorizedUsers = AuthorizationChange.applyUserAuthorization(listOf(), this.authorizations),
    authorizedGroups = AuthorizationChange.applyGroupAuthorization(listOf(), this.authorizations),
    protocol = addModification(listOf(), this.updateModification, this.state).map { it.toProtocolElement()},
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
 * Creates protocol element.
 */
fun ProtocolEntry.toProtocolElement() = ProtocolElement(
  time = this.time,
  statusType = this.state.processingType.name,
  state = this.state.state,
  username = this.username,
  logMessage = this.logMessage,
  logDetails = this.logDetails
)

/**
 * Creates the protocol.
 */
fun ProtocolElement.toProtocol() = ProtocolEntry(
  time = this.time,
  state = DataEntryStateImpl(processingType = ProcessingType.valueOf(this.statusType), state = this.state ?: ""),
  username = this.username,
  logMessage = this.logMessage,
  logDetails = this.logDetails
)
