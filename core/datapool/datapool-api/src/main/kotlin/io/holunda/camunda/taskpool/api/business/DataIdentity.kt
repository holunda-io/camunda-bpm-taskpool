package io.holunda.camunda.taskpool.api.business

/**
 * Represents an identity of a data entry.
 */
interface DataIdentity {
  /**
   * String representation of an entry.
   */
  val entryType: EntryType
  /**
   * String representation of an entry.
   */
  val entryId: EntryId

  /**
   * Retrieves the identity as a string.
   */
  fun asString() = dataIdentityString(entryType = entryType, entryId = entryId)
}
/**
 * Id of business entry.
 */
typealias EntryId = String

/**
 * Type of business entry.
 */
typealias EntryType = String

/**
 * Separator.
 */
const val DATA_IDENTITY_SEPARATOR = "#"

/**
 * Constructs the data identity.
 */
fun dataIdentityString(entryType: EntryType, entryId: EntryId) = "$entryType$DATA_IDENTITY_SEPARATOR$entryId"
