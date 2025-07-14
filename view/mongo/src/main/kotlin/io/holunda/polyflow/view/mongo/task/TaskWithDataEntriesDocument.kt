package io.holunda.polyflow.view.mongo.task

import io.holunda.polyflow.view.Task
import io.holunda.polyflow.view.mongo.data.DataEntryDocument
import org.camunda.bpm.engine.variable.Variables
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

/**
 * Document for task with data entries.
 */
@Document(collection = TaskDocument.COLLECTION)
@TypeAlias("task")
data class TaskWithDataEntriesDocument(
  @Id
  val id: String,
  val sourceReference: ReferenceDocument,
  val taskDefinitionKey: String,
  val dataEntries: List<DataEntryDocument>,
  val payload: MutableMap<String, Any> = mutableMapOf(),
  val businessKey: String? = null,
  val name: String? = null,
  val description: String? = null,
  val formKey: String? = null,
  val priority: Int? = 0,
  val createTime: Instant? = null,
  val candidateUsers: Set<String> = setOf(),
  val candidateGroups: Set<String> = setOf(),
  val assignee: String? = null,
  val owner: String? = null,
  val dueDate: Instant? = null,
  val followUpDate: Instant? = null
)

/**
 * Create a task from task document.
 */
fun TaskWithDataEntriesDocument.task() = Task(
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
  followUpDate = this.followUpDate,
  deleted = false
)

