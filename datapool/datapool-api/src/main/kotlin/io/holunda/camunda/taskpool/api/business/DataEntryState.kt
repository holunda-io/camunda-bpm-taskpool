package io.holunda.camunda.taskpool.api.business

/**
 * Compound processing state.
 */
interface DataEntryState {
  /**
   * State type.
   */
  val processingType: ProcessingType
  /**
   * Carrying business-relevant state label.
   */
  val state: String?
}

/**
 * Simple implementation of compound entry state.
 */
data class DataEntryStateImpl(
  override val processingType: ProcessingType = ProcessingType.UNDEFINED,
  override val state: String = ""
) : DataEntryState
