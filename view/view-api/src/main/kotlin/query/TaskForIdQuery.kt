package io.holunda.camunda.taskpool.view.query

import io.holunda.camunda.taskpool.view.Task

data class TaskForIdQuery(val id: String) : FilterQuery<Task> {
  override fun applyFilter(element: Task): Boolean = element.id == id
}
