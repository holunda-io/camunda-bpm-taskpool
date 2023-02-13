package io.holunda.polyflow.view.query.task

import io.holunda.polyflow.view.TaskWithDataEntries
import io.holunda.polyflow.view.query.PageableSortableQuery
import io.holunda.polyflow.view.query.QueryResult

/**
 * Result for query for multiple tasks with data entries.
 */
data class TasksWithDataEntriesQueryResult(
  override val elements: List<TaskWithDataEntries>,
  override val totalElementCount: Int = elements.size
) : QueryResult<TaskWithDataEntries, TasksWithDataEntriesQueryResult>(elements = elements, totalElementCount = totalElementCount) {
  override fun slice(query: PageableSortableQuery) = this.copy(elements = super.slice(query).elements)
}
