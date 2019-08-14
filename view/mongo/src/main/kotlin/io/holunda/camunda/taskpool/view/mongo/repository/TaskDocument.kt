package io.holunda.camunda.taskpool.view.mongo.repository

import io.holunda.camunda.taskpool.api.task.CaseReference
import io.holunda.camunda.taskpool.api.task.ProcessReference
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document(collection = "tasks")
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
)

sealed class ReferenceDocument {
  companion object {
    const val CASE = "case"
    const val PROCESS = "process"
  }

  abstract val instanceId: String
  abstract val executionId: String
  abstract val definitionId: String
  abstract val definitionKey: String
  abstract val name: String
  abstract val applicationName: String
  abstract val tenantId: String?
}

@Document(collection = "sources")
@TypeAlias(ReferenceDocument.CASE)
data class CaseReferenceDocument(
  @Id
  override val instanceId: String,
  override val executionId: String,
  override val definitionId: String,
  @Indexed
  override val definitionKey: String,
  override val name: String,
  @Indexed
  override val applicationName: String,
  @Indexed
  override val tenantId: String? = null
) : ReferenceDocument() {
  constructor(reference: CaseReference) :
    this(
      instanceId = reference.instanceId,
      executionId = reference.executionId,
      definitionId = reference.definitionId,
      definitionKey = reference.definitionKey,
      name = reference.name,
      applicationName = reference.applicationName,
      tenantId = reference.tenantId
    )
}

@Document(collection = "sources")
@TypeAlias(ReferenceDocument.PROCESS)
data class ProcessReferenceDocument(
  @Id
  override val instanceId: String,
  override val executionId: String,
  override val definitionId: String,
  @Indexed
  override val definitionKey: String,
  override val name: String,
  @Indexed
  override val applicationName: String,
  @Indexed
  override val tenantId: String? = null
) : ReferenceDocument() {

  constructor(reference: ProcessReference) :
    this(
      instanceId = reference.instanceId,
      executionId = reference.executionId,
      definitionId = reference.definitionId,
      definitionKey = reference.definitionKey,
      name = reference.name,
      applicationName = reference.applicationName,
      tenantId = reference.tenantId
    )
}
