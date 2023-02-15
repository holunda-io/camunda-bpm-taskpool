package io.holunda.polyflow.view.query.data

import io.holunda.camunda.taskpool.api.business.DataIdentity
import io.holunda.camunda.taskpool.api.business.EntryId
import io.holunda.camunda.taskpool.api.business.EntryType
import io.holunda.polyflow.view.DataEntry
import io.holunda.polyflow.view.query.FilterQuery

/**
 * Query for entry type and optional id.
 * @param entryType type of the data entry.
 * @param entryId id of the data entry.
 */
data class DataEntryForIdentityQuery(
  val entryType: EntryType,
  val entryId: EntryId,
) : FilterQuery<DataEntry> {

  /**
   * Additional convenience constructor.
   */
  constructor(
    identity: DataIdentity
  ) : this(
    entryType = identity.entryType,
    entryId = identity.entryId,
  )

  override fun applyFilter(element: DataEntry) = element.entryType == this.entryType && element.entryId == this.entryId
}




