package io.holunda.camunda.taskpool.view.mongo.repository

import io.holunda.camunda.taskpool.view.mongo.repository.DataEntryDocument.Companion.NAME
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.OffsetDateTime
import java.util.*

/**
 * Represents a business data entry as Mongo Document.
 */
@Document(collection = NAME)
@TypeAlias("data-entry")
data class DataEntryDocument(
  @Id
  val identity: String,
  @Indexed
  val entryType: String,
  val payload: Map<String, Any> = mapOf(),
  val correlations: Map<String, Any> = mapOf(),
  val type: String,
  val name: String,
  val applicationName: String,
  val description: String?,
  val state: String?,
  val statusType: String?,
  val authorizedUsers: List<String> = listOf(),
  val authorizedGroups: List<String> = listOf(),
  val protocol: List<ProtocolElement> = listOf()
) {
  companion object {
      const val NAME = "data-entries"
  }
}

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
