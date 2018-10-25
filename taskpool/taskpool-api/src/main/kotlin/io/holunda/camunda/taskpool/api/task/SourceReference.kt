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
  open val processName: String,
  open val applicationName: String
)

data class ProcessReference(
  override val instanceId: String,
  override val executionId: String,
  override val definitionId: String,
  override val definitionKey: String,
  override val processName: String,
  override val applicationName: String
): SourceReference(instanceId, executionId, definitionId,definitionKey,processName,applicationName)

data class CaseReference(
  override val instanceId: String,
  override val executionId: String,
  override val definitionId: String,
  override val definitionKey: String,
  override val processName: String,
  override val applicationName: String
): SourceReference(instanceId, executionId, definitionId,definitionKey,processName,applicationName)

