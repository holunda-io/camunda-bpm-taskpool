package io.holunda.camunda.taskpool.view

import io.holunda.camunda.taskpool.api.business.DataEntryState
import java.util.*

/**
 * Represents a protocol entry.
 */
data class ProtocolEntry(
    val time: Date,
    val state: DataEntryState,
    val username: String?,
    val logMessage: String?,
    val logDetails: String?
)
