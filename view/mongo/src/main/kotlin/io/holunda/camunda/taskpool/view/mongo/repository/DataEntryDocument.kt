package io.holunda.camunda.taskpool.view.mongo.repository

import io.holunda.camunda.taskpool.view.mongo.repository.DataEntryDocument.Companion.NAME
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

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
  val payload: Map<String, Any>
) {
  companion object {
      const val NAME = "data-entries"
  }
}
