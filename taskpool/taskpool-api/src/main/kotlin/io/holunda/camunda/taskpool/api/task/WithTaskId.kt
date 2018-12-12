package io.holunda.camunda.taskpool.api.task

/**
 * Identifies the fact that the task id is present.
 * This is a minimum requirement for the task to be identified.
 */
interface WithTaskId {
  val id: String
}
