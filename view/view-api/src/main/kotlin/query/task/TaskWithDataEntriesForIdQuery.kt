package io.holunda.camunda.taskpool.view.query.task

import io.holunda.camunda.taskpool.view.TaskWithDataEntries
import io.holunda.camunda.taskpool.view.query.FilterQuery

/**
 * Query for task with given id with correlated data entries.
 * @param id task id.
 */
data class TaskWithDataEntriesForIdQuery(val id: String) : FilterQuery<TaskWithDataEntries> {
  override fun applyFilter(element: TaskWithDataEntries): Boolean = element.task.id == id
}
