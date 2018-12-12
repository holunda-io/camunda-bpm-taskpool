package io.holunda.camunda.taskpool.api.task

/**
 * Identifies elements with form key.
 */
interface WithFormKey {
  /**
   * Optional form key.
   */
  val formKey: String?
}
