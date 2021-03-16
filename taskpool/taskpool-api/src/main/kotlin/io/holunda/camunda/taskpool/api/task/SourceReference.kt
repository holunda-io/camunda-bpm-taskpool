package io.holunda.camunda.taskpool.api.task


/**
 * Represents the source of the task.
 * Currently supported is process or case.
 */
interface SourceReference {
  /**
   * Source instance id.
   */
  val instanceId: String

  /**
   * Source execution id.
   */
  val executionId: String

  /**
   * Source definition id.
   */
  val definitionId: String

  /**
   * Source definition key.
   */
  val definitionKey: String

  /**
   * Source name.
   */
  val name: String

  /**
   * Application name.
   */
  val applicationName: String

  /**
   * Tenant id.
   */
  val tenantId: String?
}

/**
 * The source of the task is a process.
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
) : SourceReference

/**
 * * The source of the task is a case.
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
) : SourceReference

/**
 * The source of the task is no workflow engine
 */
data class GenericReference(
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
) : SourceReference

