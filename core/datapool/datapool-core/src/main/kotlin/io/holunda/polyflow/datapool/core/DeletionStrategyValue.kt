package io.holunda.polyflow.datapool.core

/**
 * Different deletion strategies.
 */
enum class DeletionStrategyValue {
  /**
   * Allow updates after deletes.
   */
  strict,

  /**
   * Enforces errors on any operation after deletion.
   */
  lax
}
