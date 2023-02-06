package io.holunda.polyflow.view.query.task

import io.holunda.polyflow.view.TaskWithDataEntries
import io.holunda.polyflow.view.query.FilterQuery


/**
 * Query for task with given id with correlated data entries.
 * @param id task id.
 */
data class TaskWithDataEntriesForIdQuery(val id: String) : FilterQuery<TaskWithDataEntries> {
  override fun applyFilter(element: TaskWithDataEntries): Boolean = TaskForIdQuery(id).applyFilter(element.task)
}
