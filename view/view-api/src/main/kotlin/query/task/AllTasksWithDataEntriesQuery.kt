package io.holunda.polyflow.view.query.task

import io.holunda.polyflow.view.TaskWithDataEntries
import io.holunda.polyflow.view.query.FilterQuery
import io.holunda.polyflow.view.query.PageableSortableQuery

/**
 * Query for tasks.
 * @param page - page to read, zero-based index.
 * @param size - size of the page
 * @param sort - optional attribute to sort by.
 * @param filters - the filters to further filter down the tasks.
 */
data class AllTasksWithDataEntriesQuery(
  override val page: Int = 0,
  override val size: Int = Int.MAX_VALUE,
  override val sort: String? = null,
  val filters: List<String> = listOf()
) : FilterQuery<TaskWithDataEntries>, PageableSortableQuery {
  override fun applyFilter(element: TaskWithDataEntries): Boolean = AllTasksQuery(page, size, sort, filters).applyFilter(element.task)
}

