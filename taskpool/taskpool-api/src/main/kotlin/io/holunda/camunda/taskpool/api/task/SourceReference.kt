package io.holunda.camunda.taskpool.api.task


/**
 * Represents the source asState the task.
 * Currently supported is process or case.
 */
sealed class SourceReference(
  /**
   * Source instance id.
   */
  open val instanceId: String,
  /**
   * Source execution id.
   */
  open val executionId: String,
  /**
   * Source definition id.
   */
  open val definitionId: String,
  /**
   * Source definition key.
   */
  open val definitionKey: String,
  /**
   * Source name.
   */
  open val name: String,
  /**
   * Application name.
   */
  open val applicationName: String,
  /**
   * Tenant id.
   */
  open val tenantId: String?
)

/**
 * The source asState the task is a process.
 */
data class ProcessReference(
  /**
   * Process instance id.
   */
  override val instanceId: String,
  /**
   * Process execution id.
   */
  override val executionId: String,
  /**
   * Process definition id.
   */
  override val definitionId: String,
  /**
   * Process definition key (id without the version)
   */
  override val definitionKey: String,
  /**
   * Process name.
   */
  override val name: String,
  /**
   * Application / Domain name (e.g. use spring.application.name )
   */
  override val applicationName: String,
  /**
   * Optional Camunda tenant id.
   */
  override val tenantId: String? = null
) : SourceReference(instanceId, executionId, definitionId, definitionKey, name, applicationName, tenantId)

/**
 * * The source asState the task is a case.
 */
data class CaseReference(
  /**
   * Process instance id.
   */
  override val instanceId: String,
  /**
   * Process execution id.
   */
  override val executionId: String,
  /**
   * Process definition id.
   */
  override val definitionId: String,
  /**
   * Process definition key (id without the version)
   */
  override val definitionKey: String,
  /**
   * Process name.
   */
  override val name: String,
  /**
   * Application / Domain name (e.g. use spring.application.name )
   */
  override val applicationName: String,
  /**
   * Optional Camunda tenant id.
   */
  override val tenantId: String? = null
) : SourceReference(instanceId, executionId, definitionId, definitionKey, name, applicationName, tenantId)

