package io.holunda.camunda.taskpool.view.query.data

import io.holunda.camunda.taskpool.view.DataEntry
import io.holunda.camunda.taskpool.view.query.FilterQuery
import io.holunda.camunda.taskpool.view.query.PageableSortableQuery

/**
 * Generic queries data entries.
 * @param page current page.
 * @param size page size.
 * @param sort sort of data entries.
 * @param filters list of filters.
 */
data class DataEntriesQuery(
  override val page: Int = 1,
  override val size: Int = Int.MAX_VALUE,
  override val sort: String? = null,
  val filters: List<String> = listOf()

) : FilterQuery<DataEntry>, PageableSortableQuery {
  override fun applyFilter(element: DataEntry): Boolean = false
}
