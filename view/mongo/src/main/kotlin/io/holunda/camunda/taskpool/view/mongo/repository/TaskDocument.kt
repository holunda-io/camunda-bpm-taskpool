package io.holunda.camunda.taskpool.view.mongo.repository

import io.holunda.camunda.taskpool.api.task.CaseReference
import io.holunda.camunda.taskpool.api.task.ProcessReference
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.DBRef
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
  val name: String? = null,
  val description: String? = null,
  val formKey: String? = null,
  val priority: Int? = 0,
  val createTime: Date? = null,
  val candidateUsers: Set<String> = setOf(),
  val candidateGroups: Set<String> = setOf(),
  val assignee: String? = null,
  val owner: String? = null,
  val dueDate: Date? = null,
  val followUpDate: Date? = null
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
  override val definitionKey: String,
  override val name: String,
  override val applicationName: String,
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
  override val definitionKey: String,
  override val name: String,
  override val applicationName: String,
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
