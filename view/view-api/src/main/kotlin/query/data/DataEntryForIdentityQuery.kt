package io.holunda.polyflow.view.query.data

import io.holunda.camunda.taskpool.api.business.DataIdentity
import io.holunda.camunda.taskpool.api.business.EntryId
import io.holunda.camunda.taskpool.api.business.EntryType
import io.holunda.polyflow.view.DataEntry
import io.holunda.polyflow.view.query.FilterQuery

/**
 * Query for entry type and optional id.
 * @param identity identity to query for.
 */
data class DataEntryForIdentityQuery(
  val identity: DataIdentity,
) : FilterQuery<DataEntry> {

  /**
   * Compatibility constructor to avoid compile errors.
   */
  constructor(
    entryType: EntryType,
    entryId: EntryId,
  ) : this(
    identity = QueryDataIdentity(
      entryType = entryType,
      entryId = entryId,
    )
  )

  override fun applyFilter(element: DataEntry) = element.entryType == this.identity.entryType && element.entryId == this.identity.entryId
}




