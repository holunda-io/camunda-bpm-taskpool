package io.holunda.polyflow.view.query.data

import io.holunda.camunda.taskpool.api.business.DataIdentity
import io.holunda.camunda.taskpool.api.business.EntryId
import io.holunda.camunda.taskpool.api.business.EntryType
import io.holunda.polyflow.view.DataEntry
import io.holunda.polyflow.view.query.FilterQuery
import io.holunda.polyflow.view.query.PageableSortableQuery

/**
 * Query for entry type and optional id.
 * @param page current page, zero-based index.
 * @param size page size
 */
data class DataEntryForIdentityQuery(
  val entryType: EntryType,
  val entryId: EntryId? = null,
  override val page: Int = 0,
  override val size: Int = Int.MAX_VALUE,
  override val sort: String? = null
) : FilterQuery<DataEntry>, PageableSortableQuery {


  override fun applyFilter(element: DataEntry) =
// entry type
    element.entryType == this.entryType
// if id is specified, applyFilter by it
      && (this.entryId == null || element.entryId == this.entryId)

  /**
   * Construct query identity out of given query parameters.
   */
  fun identity(): DataIdentity = QueryDataIdentity(entryType = entryType, entryId = entryId
    ?: throw IllegalArgumentException("Entry id is not specified. This query can't be used as data entry identity."))

}




