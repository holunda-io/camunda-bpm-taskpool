package io.holunda.camunda.taskpool.view.query.task

import io.holunda.camunda.taskpool.view.Task
import io.holunda.camunda.taskpool.view.query.FilterQuery

/**
 * Query for task with a given id.
 * @param id task id.
 */
data class TaskForIdQuery(val id: String) : FilterQuery<Task> {
  override fun applyFilter(element: Task): Boolean = element.id == id
}
