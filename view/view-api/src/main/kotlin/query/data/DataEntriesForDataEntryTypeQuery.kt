package io.holunda.polyflow.view.query.data

import io.holunda.camunda.taskpool.api.business.EntryType
import io.holunda.polyflow.view.DataEntry
import io.holunda.polyflow.view.query.FilterQuery
import io.holunda.polyflow.view.query.PageableSortableQuery

/**
 * Query by entry type.
 * @param entryType type of data entry.
 * @param page current page, zero-based index.
 * @param size page size
 */
data class DataEntriesForDataEntryTypeQuery(
  val entryType: EntryType,
  override val page: Int = 0,
  override val size: Int = Int.MAX_VALUE,
  override val sort: List<String> = listOf()
) : FilterQuery<DataEntry>, PageableSortableQuery {

  @Deprecated("Please use other constructor setting sort as List<String>")
  constructor(entryType: EntryType, page: Int = 0, size: Int = Int.MAX_VALUE, sort: String) : this(
    entryType = entryType,
    page = page,
    size = size,
    sort = if (sort.isBlank()) listOf() else listOf(sort),
  )

  override fun applyFilter(element: DataEntry) = element.entryType == this.entryType
}




