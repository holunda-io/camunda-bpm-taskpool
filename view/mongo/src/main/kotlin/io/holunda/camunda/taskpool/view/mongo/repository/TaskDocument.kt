package io.holunda.camunda.taskpool.view.mongo.repository

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
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
  val createTime: Date? = null,
  @Indexed
  val candidateUsers: Set<String> = setOf(),
  @Indexed
  val candidateGroups: Set<String> = setOf(),
  @Indexed
  val assignee: String? = null,
  @Indexed
  val owner: String? = null,
  @Indexed
  val dueDate: Date? = null,
  @Indexed
  val followUpDate: Date? = null,
  val deleted: Boolean = false
) {
  companion object {
    const val COLLECTION = "tasks"
  }
}

