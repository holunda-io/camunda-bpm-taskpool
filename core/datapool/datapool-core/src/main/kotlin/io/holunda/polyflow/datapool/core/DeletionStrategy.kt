package io.holunda.polyflow.datapool.core

/**
 * Describes how deleted data entries should be handled.
 */
interface DeletionStrategy {
  /**
   * If set to true, the update event sent to the aggregate will result in AggregateDeletedException.
   * If set to false, the update of deleted data entry will undelete it and update the properties.
   */
  fun strictMode(): Boolean
}