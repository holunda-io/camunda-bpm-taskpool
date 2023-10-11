package io.holunda.polyflow.view.query.data

import io.holunda.polyflow.view.DataEntry
import io.holunda.polyflow.view.auth.User
import io.holunda.polyflow.view.filter.createDataEntryPredicates
import io.holunda.polyflow.view.filter.filterByPredicate
import io.holunda.polyflow.view.filter.toCriteria
import io.holunda.polyflow.view.query.FilterQuery
import io.holunda.polyflow.view.query.PageableSortableQuery

/**
 * Generic queries data entries.
 * @param page current page.
 * @param size page size.
 * @param sort sort of data entries.
 * @param filters list of filters.
 */
data class DataEntriesQuery(
  override val page: Int = 0,
  override val size: Int = Int.MAX_VALUE,
  override val sort: List<String> = listOf(),
  val filters: List<String> = listOf()
) : FilterQuery<DataEntry>, PageableSortableQuery {

  @Deprecated("Please use other constructor setting sort as List<String>")
  constructor(page: Int = 0, size: Int = Int.MAX_VALUE, sort: String?, filters: List<String> = listOf()): this(
    page = page,
    size = size,
    sort = if (sort.isNullOrBlank()) {
      listOf()
    } else {
      listOf(sort)
    },
    filters = filters
  )

  // jackson serialization works because delegate property is private
  private val predicates by lazy { createDataEntryPredicates(toCriteria(filters)) }

  override fun applyFilter(element: DataEntry): Boolean = filterByPredicate(element, predicates)
}
