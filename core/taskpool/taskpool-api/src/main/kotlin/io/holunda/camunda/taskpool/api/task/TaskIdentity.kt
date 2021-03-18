package io.holunda.camunda.taskpool.api.task

/**
 * Task identity.
 */
interface TaskIdentity : WithTaskId {
  /**
   * Task definition key.
   */
  val taskDefinitionKey: String
  /**
   * Task source reference.
   */
  val sourceReference: SourceReference
}
