package io.holunda.camunda.taskpool.view.query.task

import io.holunda.camunda.taskpool.view.TaskWithDataEntries
import io.holunda.camunda.taskpool.view.query.PageableSortableQuery
import io.holunda.camunda.taskpool.view.query.QueryResult

/**
 * Result for query for multiple tasks with data entries.
 */
data class TasksWithDataEntriesQueryResult(
  override val elements: List<TaskWithDataEntries>
) : QueryResult<TaskWithDataEntries, TasksWithDataEntriesQueryResult>(elements = elements) {
  override fun slice(query: PageableSortableQuery) = this.copy(elements = super.slice(query).elements)
}
