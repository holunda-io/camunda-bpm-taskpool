package io.holunda.polyflow.view.jpa

/**
 * Helper multi-valued class to support count by application name queries.
 */
data class CountByApplication(
  /**
   * application name (grouping criteria).
   */
  val applicationName: String,
  /**
   * Count.
   */
  val count: Long
)
