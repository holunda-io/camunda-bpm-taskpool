package io.holunda.polyflow.view

import io.holunda.camunda.taskpool.api.business.DataEntryState
import java.time.Instant
import java.util.*

/**
 * Represents a protocol entry.
 */
data class ProtocolEntry(
  val time: Instant,
  val state: DataEntryState,
  val username: String?,
  val logMessage: String?,
  val logDetails: String?
)
