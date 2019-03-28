package io.holunda.camunda.taskpool.view.mongo.repository

import io.holunda.camunda.taskpool.api.business.DATA_IDENTITY_SEPARATOR
import io.holunda.camunda.taskpool.api.business.EntryId
import io.holunda.camunda.taskpool.api.business.dataIdentity
import io.holunda.camunda.taskpool.api.task.CaseReference
import io.holunda.camunda.taskpool.api.task.ProcessReference
import io.holunda.camunda.taskpool.api.task.SourceReference
import io.holunda.camunda.taskpool.view.Task
import io.holunda.camunda.taskpool.view.TaskWithDataEntries
import org.camunda.bpm.engine.variable.Variables

/**
 * Create a task document from task.
 */
fun Task.taskDocument() = TaskDocument(
  id = this.id,
  sourceReference = when (val reference = this.sourceReference) {
    is ProcessReference -> ProcessReferenceDocument(reference)
    is CaseReference -> CaseReferenceDocument(reference)
  },
  taskDefinitionKey = this.taskDefinitionKey,
  payload = this.payload.toMutableMap(),
  // FIXME: remove
  correlations = this.correlations.toMutableMap(),
  dataEntriesRefs = this.correlations.map { dataIdentity(entryType = it.key, entryId = it.value as EntryId) }.toSet(),
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
  followUpDate = this.followUpDate
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
    is CaseReferenceDocument -> ProcessReference(
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
        payload = org.camunda.bpm.engine.variable.Variables.fromMap(payload)
      )
    }
  } else {
    throw IllegalArgumentException("Identity could not be split into entry type and id, because it doesn't contain the '$DATA_IDENTITY_SEPARATOR'. Value was $identity")
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
