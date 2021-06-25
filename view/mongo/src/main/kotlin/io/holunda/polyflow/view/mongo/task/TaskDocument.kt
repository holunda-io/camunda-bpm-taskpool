package io.holunda.polyflow.view.mongo.repository

import io.holunda.camunda.taskpool.api.business.EntryId
import io.holunda.camunda.taskpool.api.business.dataIdentityString
import io.holunda.camunda.taskpool.api.task.CaseReference
import io.holunda.camunda.taskpool.api.task.GenericReference
import io.holunda.camunda.taskpool.api.task.ProcessReference
import io.holunda.polyflow.view.Task
import org.camunda.bpm.engine.variable.Variables
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant
import java.util.*

/**
 * Task document.
 */
@Document(collection = TaskDocument.COLLECTION)
@TypeAlias("task")
data class TaskDocument(
  @Id
  val id: String,
  val sourceReference: ReferenceDocument,
  val taskDefinitionKey: String,
  val payload: MutableMap<String, Any> = mutableMapOf(),
  val correlations: MutableMap<String, Any> = mutableMapOf(),
  val dataEntriesRefs: Set<String> = setOf(),
  val businessKey: String? = null,
  @Indexed
  val name: String? = null,
  val description: String? = null,
  val formKey: String? = null,
  @Indexed
  val priority: Int? = 0,
  @Indexed
  val createTime: Instant? = null,
  @Indexed
  val candidateUsers: Set<String> = setOf(),
  @Indexed
  val candidateGroups: Set<String> = setOf(),
  @Indexed
  val assignee: String? = null,
  @Indexed
  val owner: String? = null,
  @Indexed
  val dueDate: Instant? = null,
  @Indexed
  val followUpDate: Instant? = null,
  val deleted: Boolean = false
) {
  companion object {
    const val COLLECTION = "tasks"
  }
}

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

