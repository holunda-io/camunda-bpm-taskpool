package io.holunda.camunda.taskpool.view.query

import io.holunda.camunda.taskpool.view.TaskWithDataEntries

data class TaskWithDataEntriesForIdQuery(val id: String) : FilterQuery<TaskWithDataEntries> {
  override fun applyFilter(element: TaskWithDataEntries): Boolean = element.task.id == id
}
