package io.holunda.polyflow.view.query.task

import io.holunda.polyflow.view.TaskWithDataEntries
import io.holunda.polyflow.view.query.PageableSortableQuery
import io.holunda.polyflow.view.query.QueryResult

/**
 * Result for query for multiple tasks with data entries.
 */
data class TasksWithDataEntriesQueryResult(
  override val elements: List<TaskWithDataEntries>
) : QueryResult<TaskWithDataEntries, TasksWithDataEntriesQueryResult>(elements = elements) {
  override fun slice(query: PageableSortableQuery) = this.copy(elements = super.slice(query).elements)
}
