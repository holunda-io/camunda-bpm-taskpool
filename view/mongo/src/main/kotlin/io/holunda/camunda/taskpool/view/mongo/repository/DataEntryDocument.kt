package io.holunda.camunda.taskpool.view.mongo.repository

import io.holunda.camunda.taskpool.api.business.DATA_IDENTITY_SEPARATOR
import io.holunda.camunda.taskpool.view.DataEntry
import org.camunda.bpm.engine.variable.Variables
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document

/**
 * Represents a business data entry as Mongo Document.
 */
@Document(collection = "data-entries")
@TypeAlias("data-entry")
data class DataEntryDocument(
  @Id
  val identity: String,
  val payload: Map<String, Any>
) {
  fun dataEntry() =
    if (identity.contains(DATA_IDENTITY_SEPARATOR)) {
      with(identity.split(DATA_IDENTITY_SEPARATOR)) {
        DataEntry(
          entryType = this[0],
          entryId = this[1],
          payload = Variables.fromMap(payload)
        )
      }
    } else {
      throw IllegalArgumentException("Identity could not be split into entry type and id, because it doesn't contain the '$DATA_IDENTITY_SEPARATOR'. Value was $identity")
    }
}
