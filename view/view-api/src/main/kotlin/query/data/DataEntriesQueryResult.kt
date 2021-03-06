package io.holunda.polyflow.view.query.data

import io.holunda.polyflow.view.DataEntry
import io.holunda.polyflow.view.query.PageableSortableQuery
import io.holunda.polyflow.view.query.QueryResult

/**
 * Results of a query for multiple data entries.
 */
data class DataEntriesQueryResult(
  override val elements: List<DataEntry>
) : QueryResult<DataEntry, DataEntriesQueryResult>(elements = elements) {

  override fun slice(query: PageableSortableQuery) = this.copy(elements = super.slice(query).elements)
}
