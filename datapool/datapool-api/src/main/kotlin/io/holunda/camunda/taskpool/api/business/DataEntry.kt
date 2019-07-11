package io.holunda.camunda.taskpool.api.business

import io.holunda.camunda.taskpool.api.business.Modification.Companion.NONE
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables
import java.time.OffsetDateTime

/**
 * Represents a data entry.
 */
data class DataEntry(

  /**
   * Entry type
   */
  val entryType: EntryType,
  /**
   * Entry id.
   */
  val entryId: EntryId,
  /**
   * Human readable type e.g. "purchase order"
   */
  val type: String,
  /**
   * Application name (origin)
   */
  val applicationName: String,
  /**
   * Human readable identifier or name, e.g. "BANF-4711 - TV for meeting room"
   */
  val name: String,
  /**
   * Correlations.
   */
  val correlations: CorrelationMap = newCorrelations(),
  /**
   * Payload.
   */
  val payload: VariableMap = Variables.createVariables(),
  /**
   * Human readable description, e.g. "TV in meeting room Hamburg is broken and a new one should be installed."
   */
  val description: String? = null,
  /**
   * State of data entry.
   */
  val state: DataEntryState = ProcessingType.UNDEFINED.of(""),
  /**
   * Modification information.
   */
  val modification: Modification = NONE,
  /**
   * Authorization information.
   */
  val authorizations: List<AuthorizationChange> = listOf(),
  /**
   * Form key.
   */
  val formKey: String? = null
)


interface DataEntryState {
  val processingType: ProcessingType
  val state: String?
}

data class DataEntryStateImpl(
  override val processingType: ProcessingType = ProcessingType.UNDEFINED,
  override val state: String = ""
) : DataEntryState

enum class ProcessingType {
  PRELIMINARY,
  IN_PROGRESS,
  COMPLETED,
  CANCELLED,
  UNDEFINED;

  fun of(state: String = "") = DataEntryStateImpl(processingType = this, state = state)
}


data class Modification(
  /**
   * Time of update
   */
  val time: OffsetDateTime? = null,
  /**
   * Username of the user who updated the business entry.
   */
  val username: String? = null,
  /**
   * Log entry for the update.
   */
  val log: String? = null,

  /**
   * Log entry details.
   */
  val logNotes: String? = null
) {
  companion object {
    val NONE = Modification()

    fun now() = Modification(time = OffsetDateTime.now())
  }
}


sealed class AuthorizationChange {

  companion object {
    @JvmStatic
    fun addUser(username: String): AuthorizationChange = AddAuthorization(authorizedUsers = listOf(username))

    @JvmStatic
    fun removeUser(username: String): AuthorizationChange = RemoveAuthorization(authorizedUsers = listOf(username))

    @JvmStatic
    fun addGroup(groupName: String): AuthorizationChange = AddAuthorization(authorizedGroups = listOf(groupName))

    @JvmStatic
    fun removeGroup(groupName: String): AuthorizationChange = RemoveAuthorization(authorizedGroups = listOf(groupName))

    @JvmStatic
    fun applyUserAuthorization(authorizedUsers: List<String>, authorizationChanges: List<AuthorizationChange>): List<String> {

      val usersToRemove = authorizationChanges.filterIsInstance<RemoveAuthorization>().flatMap { it.authorizedUsers }
      val usersToAdd = authorizationChanges.filterIsInstance<AddAuthorization>().flatMap { it.authorizedUsers }
      val mutable = authorizedUsers.toMutableList()
      mutable.addAll(usersToAdd)
      mutable.removeAll(usersToRemove)

      return mutable.toList()
    }

    @JvmStatic
    fun applyGroupAuthorization(authorizedGroups: List<String>, authorizationChanges: List<AuthorizationChange>): List<String> {

      val groupsToRemove = authorizationChanges.filterIsInstance<RemoveAuthorization>().flatMap { it.authorizedGroups }
      val groupsToAdd = authorizationChanges.filterIsInstance<AddAuthorization>().flatMap { it.authorizedGroups }
      val mutable = authorizedGroups.toMutableList()
      mutable.addAll(groupsToAdd)
      mutable.removeAll(groupsToRemove)

      return mutable.toList()
    }

  }
}

/**
 * Grants access to data entry.
 */
data class AddAuthorization(
  /**
   * List of authorized users to grant access.
   */
  val authorizedUsers: List<String> = listOf(),
  /**
   * List of authorized groups to grant access.
   */
  val authorizedGroups: List<String> = listOf()
) : AuthorizationChange()

/**
 * Removes access to data entry.
 */
data class RemoveAuthorization(
  /**
   * List of authorized users to grant access.
   */
  val authorizedUsers: List<String> = listOf(),
  /**
   * List of authorized groups to grant access.
   */
  val authorizedGroups: List<String> = listOf()
) : AuthorizationChange()
