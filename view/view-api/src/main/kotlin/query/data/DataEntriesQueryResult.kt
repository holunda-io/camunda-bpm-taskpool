package io.holunda.camunda.taskpool.view.query.data

import io.holunda.camunda.taskpool.view.DataEntry
import io.holunda.camunda.taskpool.view.query.PageableSortableQuery
import io.holunda.camunda.taskpool.view.query.QueryResult

/**
 * Results of a query for multiple data entries.
 */
data class DataEntriesQueryResult(
  override val elements: List<DataEntry>
) : QueryResult<DataEntry, DataEntriesQueryResult>(elements = elements) {
  override fun slice(query: PageableSortableQuery): DataEntriesQueryResult {
    return DataEntriesQueryResult(elements = super.slice(query).elements)
  }
}
