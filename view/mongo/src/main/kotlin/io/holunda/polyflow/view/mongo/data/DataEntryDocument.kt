package io.holunda.polyflow.view.mongo.data

import io.holunda.camunda.taskpool.api.business.DATA_IDENTITY_SEPARATOR
import io.holunda.camunda.taskpool.api.business.DataEntryState
import io.holunda.camunda.taskpool.api.business.ProcessingType
import io.holunda.polyflow.view.DataEntry
import io.holunda.polyflow.view.mongo.data.DataEntryDocument.Companion.COLLECTION
import org.camunda.bpm.engine.variable.Variables
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
  @Deprecated(message = "Please use authorizedPrincipals instead", replaceWith = ReplaceWith("DataEntryDocument.authorizedPrincipals"))
  private val authorizedUsers: Set<String>,
  @Deprecated(message = "Please use authorizedPrincipals instead", replaceWith = ReplaceWith("DataEntryDocument.authorizedPrincipals"))
  private val authorizedGroups: Set<String>,

// Mongo DB permits only one array-type field in a compound index. To use an index for the search, we need to put users and groups together in one array that we can index. See #367.
  val authorizedPrincipals: Set<String> = authorizedPrincipals(authorizedUsers, authorizedGroups),
  val createdDate: Instant,
  val lastModifiedDate: Instant,
  val protocol: List<ProtocolElement> = listOf()
) {
  companion object {
    const val COLLECTION = "data-entries"
    const val AUTHORIZED_ENTITY_PREFIX_USER = "user:"
    const val AUTHORIZED_ENTITY_PREFIX_GROUP = "group:"

    /**
     * Create a merged `authorizedPrinipals` set out of `authorizedUsers` and `authorizedGroups`.
     * Each user is prefixed with "user:" and each group with "group:".
     */
    fun authorizedPrincipals(authorizedUsers: Set<String>, authorizedGroups: Set<String>) =
      (authorizedUsers.map { "$AUTHORIZED_ENTITY_PREFIX_USER$it" } + authorizedGroups.map { "$AUTHORIZED_ENTITY_PREFIX_GROUP$it" }).toSet()
  }

  /**
   * Retrieves authorized users by parsing authorized principals back.
   */
  fun getAuthorizedUsers() =
    authorizedPrincipals.filter { it.startsWith(AUTHORIZED_ENTITY_PREFIX_USER) }.map { it.substring(AUTHORIZED_ENTITY_PREFIX_USER.length) }.toSet()

  /**
   * Retrieves authorized groups by parsing authorized principals back.
   */
  fun getAuthorizedGroups() =
    authorizedPrincipals.filter { it.startsWith(AUTHORIZED_ENTITY_PREFIX_GROUP) }.map { it.substring(AUTHORIZED_ENTITY_PREFIX_GROUP.length) }.toSet()
}


/**
 * Constructs a data entry out of Data Entry Document.
 */
fun DataEntryDocument.dataEntry() =
  if (identity.contains(DATA_IDENTITY_SEPARATOR)) {
    with(identity.split(DATA_IDENTITY_SEPARATOR)) {
      DataEntry(
        entryType = this[0],
        entryId = this[1],
        payload = Variables.fromMap(payload),
        correlations = Variables.fromMap(correlations),
        name = name,
        description = description,
        type = type,
        authorizedUsers = getAuthorizedUsers(),
        authorizedGroups = getAuthorizedGroups(),
        applicationName = applicationName,
        state = mapState(state, statusType),
        protocol = protocol.map { it.toProtocol() }
      )
    }
  } else {
    throw IllegalArgumentException("Identity could not be split into entry type and id, because it doesn't contain the '$DATA_IDENTITY_SEPARATOR'. Value was $identity")
  }

/**
 * Maps the state.
 */
fun mapState(state: String?, statusType: String?): DataEntryState = if (state != null) {
  when (statusType) {
    "PRELIMINARY" -> ProcessingType.PRELIMINARY.of(state)
    "IN_PROGRESS" -> ProcessingType.IN_PROGRESS.of(state)
    "COMPLETED" -> ProcessingType.COMPLETED.of(state)
    "CANCELLED" -> ProcessingType.CANCELLED.of(state)
    else -> ProcessingType.UNDEFINED.of(state)
  }
} else {
  ProcessingType.UNDEFINED.of("")
}
