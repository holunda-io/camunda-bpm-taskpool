package io.holunda.camunda.taskpool.api.task


/**
 * Represents the source of the task.
 * Currently supported is process or case.
 */
sealed class SourceReference(
  open val instanceId: String,
  open val executionId: String,
  open val definitionId: String,
  open val definitionKey: String,
  open val name: String,
  open val applicationName: String,
  open val tenantId: String?
)

/**
 * The source of the task is a process
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

data class CaseReference(
  override val instanceId: String,
  override val executionId: String,
  override val definitionId: String,
  override val definitionKey: String,
  override val name: String,
  override val applicationName: String,
  override val tenantId: String? = null
) : SourceReference(instanceId, executionId, definitionId, definitionKey, name, applicationName, tenantId)

