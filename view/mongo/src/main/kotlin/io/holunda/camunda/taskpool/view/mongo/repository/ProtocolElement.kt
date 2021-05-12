package io.holunda.camunda.taskpool.view.mongo.repository

import java.util.*

/**
 * Element from the protocol.
 */
data class ProtocolElement(
  val time: Date,
  val statusType: String,
  val state: String?,
  val username: String?,
  val logMessage: String?,
  val logDetails: String?
)
