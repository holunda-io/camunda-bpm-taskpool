package io.holunda.camunda.taskpool.api.task

interface TaskIdentity {
  val id: String
  val taskDefinitionKey: String
  val sourceReference: SourceReference
}

/**
 * Represents the source of the task.
 * Currently supported is process or case.
 */
sealed class SourceReference {
  abstract val instanceId: String
  abstract val executionId: String
  abstract val definitionId: String
  abstract val definitionKey: String
  abstract val processName: String
  abstract val applicationName: String
}

data class ProcessReference(
  override val instanceId: String,
  override val executionId: String,
  override val definitionId: String,
  override val definitionKey: String,
  override val processName: String,
  override val applicationName: String
): SourceReference()

data class CaseReference(
  override val instanceId: String,
  override val executionId: String,
  override val definitionId: String,
  override val definitionKey: String,
  override val processName: String,
  override val applicationName: String
): SourceReference()

