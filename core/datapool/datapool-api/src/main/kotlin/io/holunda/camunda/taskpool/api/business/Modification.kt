package io.holunda.camunda.taskpool.api.business

import java.time.OffsetDateTime

/**
 * Represents the auditable reason for data entry modification.
 */
data class Modification(
  /**
   * Time of update
   */
  val time: OffsetDateTime = OffsetDateTime.now(),
  /**
   * Username of the user who updated the business entry.
   */
  val username: String? = null,
  /**
   * Log entry for the update.
   */
  val log: String? = null,

  /**
   * Log entry details.
   */
  val logNotes: String? = null
) {
  companion object {
    /**
     * No modification null-object.
     */
    val NONE = Modification()

    /**
     * Modification executed now.
     */
    fun now() = Modification(time = OffsetDateTime.now())
  }
}
