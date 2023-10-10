package io.holunda.polyflow.view.query.task

import io.holunda.polyflow.view.Task
import io.holunda.polyflow.view.query.FilterQuery
import io.holunda.polyflow.view.query.PageableSortableQuery

/**
 * Query for all tasks.
 * @param page - page to read, zero-based index.
 * @param size - size of the page
 * @param sort - optional attribute to sort by.
 * @param filters - the filters to further filter down the tasks.
 */
data class AllTasksQuery(
  override val page: Int = 0,
  override val size: Int = Int.MAX_VALUE,
  override val sort: List<String> = listOf(),
  override val filters: List<String> = listOf()
) : PageableSortableFilteredTaskQuery {

  @Deprecated("Please use other constructor setting sort as List<String>")
  constructor(page: Int = 0, size: Int = Int.MAX_VALUE, sort: String, filters: List<String> = listOf()): this(
    page = page,
    size = size,
    sort = if (sort.isBlank()) listOf() else listOf(sort),
    filters = filters
  )

  override fun applyFilter(element: Task): Boolean = true
}

