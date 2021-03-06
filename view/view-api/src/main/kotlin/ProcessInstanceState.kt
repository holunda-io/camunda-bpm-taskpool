package io.holunda.polyflow.view

/**
 * Process instance state.
 */
enum class ProcessInstanceState {
  /**
   * Process is running.
   */
  RUNNING,

  /**
   * Process is finished.
   */
  FINISHED,

  /**
   * Process is cancelled.
   */
  CANCELLED,

  /**
   * Process is suspended.
   */
  SUSPENDED
}
