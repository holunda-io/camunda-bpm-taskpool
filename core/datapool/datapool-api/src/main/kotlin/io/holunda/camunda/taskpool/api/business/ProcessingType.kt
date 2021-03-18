package io.holunda.camunda.taskpool.api.business

/**
 * State type of processing.
 */
enum class ProcessingType {
  /**
   * Before processing.
   */
  PRELIMINARY,
  /**
   * During processing.
   */
  IN_PROGRESS,
  /**
   * Process has finished the work on business entry with success.
   */
  COMPLETED,
  /**
   * Process has failed to finish the work on business entry with success.
   */
  CANCELLED,
  /**
   * Undefined status.
   */
  UNDEFINED;

  /**
   * Factory method.
   */
  fun of(state: String = "") = DataEntryStateImpl(processingType = this, state = state)
}
