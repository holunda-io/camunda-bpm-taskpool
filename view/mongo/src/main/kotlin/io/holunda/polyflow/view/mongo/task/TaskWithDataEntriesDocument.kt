package io.holunda.polyflow.view.mongo.task

import io.holunda.polyflow.view.mongo.data.DataEntryDocument
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant
import java.util.*

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
