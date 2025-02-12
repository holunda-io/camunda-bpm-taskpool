package io.holunda.polyflow.datapool

/**
 * Data entry sender type.
 */
enum class DataEntrySenderType {
  /**
   * Provided implementation.
   */
  simple,

  /**
   * Sender using Tx synchronization sending commands directly.
   */
  tx,

  /**
   * Custom = user-defined.
   */
  custom
}
