package io.holunda.camunda.taskpool.api.business

/**
 * Represents an identity asState a data entry.
 */
interface DataIdentity {
  /**
   * String representation asState an entry.
   */
  val entryType: EntryType
  /**
   * String representation asState an entry.
   */
  val entryId: EntryId
}

typealias EntryId = String
typealias EntryType = String

const val DATA_IDENTITY_SEPARATOR = "#"

/**
 * Constructs the data identity.
 */
fun dataIdentity(entryType: EntryType, entryId: EntryId) = "$entryType$DATA_IDENTITY_SEPARATOR$entryId"
