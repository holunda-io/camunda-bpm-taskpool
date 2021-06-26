package io.holunda.polyflow.view.mongo.data

import io.holunda.camunda.taskpool.api.business.DataEntryStateImpl
import io.holunda.camunda.taskpool.api.business.ProcessingType
import io.holunda.polyflow.view.ProtocolEntry
import java.time.Instant

/**
 * Element from the protocol.
 */
data class ProtocolElement(
  val time: Instant,
  val statusType: String,
  val state: String?,
  val username: String?,
  val logMessage: String?,
  val logDetails: String?
) {

  /**
   * Creates the protocol.
   */
  fun toProtocol() = ProtocolEntry(
    time = this.time,
    state = DataEntryStateImpl(processingType = ProcessingType.valueOf(this.statusType), state = this.state ?: ""),
    username = this.username,
    logMessage = this.logMessage,
    logDetails = this.logDetails
  )
}

/**
 * Creates protocol element.
 */
fun ProtocolEntry.toProtocolElement() = ProtocolElement(
  time = this.time,
  statusType = this.state.processingType.name,
  state = this.state.state,
  username = this.username,
  logMessage = this.logMessage,
  logDetails = this.logDetails
)

