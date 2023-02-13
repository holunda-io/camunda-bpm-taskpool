package io.holunda.polyflow.view.query.data

import io.holunda.polyflow.view.DataEntry
import io.holunda.polyflow.view.query.PageableSortableQuery
import io.holunda.polyflow.view.query.QueryResult

/**
 * Results of a query for multiple data entries.
 */
data class DataEntriesQueryResult(
  override val elements: List<DataEntry>,
  override val totalElementCount: Int = elements.size
) : QueryResult<DataEntry, DataEntriesQueryResult>(elements = elements, totalElementCount = totalElementCount) {

  override fun slice(query: PageableSortableQuery) = this.copy(elements = super.slice(query).elements)
}
