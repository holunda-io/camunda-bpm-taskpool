package io.holunda.camunda.taskpool.view.mongo.repository

import io.holunda.camunda.taskpool.view.mongo.repository.DataEntryDocument.Companion.COLLECTION
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

/**
 * Represents a business data entry as Mongo Document.
 */
@Document(collection = COLLECTION)
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
  // Mongo DB permits only one array-type field in a compound index. To use an index for the search, we need to put users and groups together in one array that we can index. See #367.
  val authorizedEntities: List<String> = authorizedEntities(authorizedUsers, authorizedGroups),
  val createdDate: Instant,
  val lastModifiedDate: Instant,
  val protocol: List<ProtocolElement> = listOf()
) {
  companion object {
    const val COLLECTION = "data-entries"
    const val AUTHORIZED_ENTITY_PREFIX_USER = "user"
    const val AUTHORIZED_ENTITY_PREFIX_GROUP = "group"

    /**
     * Create a merged `authorizedEntities` collection out of `authorizedUsers` and `authorizedGroups`. Each user is prefixed with "user:" and each group with
     * "group:".
     */
    fun authorizedEntities(authorizedUsers: List<String>, authorizedGroups: List<String>) =
      authorizedUsers.map { "$AUTHORIZED_ENTITY_PREFIX_USER:$it" } + authorizedGroups.map { "$AUTHORIZED_ENTITY_PREFIX_GROUP:$it" }
  }
}

