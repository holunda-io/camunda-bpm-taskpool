package io.holunda.polyflow.view.mongo.task

import io.holunda.camunda.taskpool.api.task.CaseReference
import io.holunda.camunda.taskpool.api.task.GenericReference
import io.holunda.camunda.taskpool.api.task.ProcessReference
import io.holunda.camunda.taskpool.api.task.SourceReference
import io.holunda.polyflow.view.mongo.task.ReferenceDocument.Companion.COLLECTION
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

/**
 * Reference.
 */
sealed class ReferenceDocument {
  companion object {

    const val COLLECTION = "sources"

    const val CASE = "case"
    const val PROCESS = "process"
    const val GENERIC = "generic"
  }

  abstract val instanceId: String
  abstract val executionId: String
  abstract val definitionId: String
  abstract val definitionKey: String
  abstract val name: String
  abstract val applicationName: String
  abstract val tenantId: String?
}

/**
 * (Camunda) CMMN Case reference.
 */
@Document(collection = COLLECTION)
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

/**
 * (Camunda) BPMN Process reference.
 */
@Document(collection = COLLECTION)
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

/**
 * Generic reference.
 */
@Document(collection = COLLECTION)
@TypeAlias(ReferenceDocument.GENERIC)
data class GenericReferenceDocument(
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

  constructor(reference: GenericReference) :
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

