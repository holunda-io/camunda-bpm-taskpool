package io.holunda.polyflow.view.mongo.repository

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
)
