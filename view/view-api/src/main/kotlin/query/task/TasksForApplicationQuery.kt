package io.holunda.polyflow.view.query.task

import io.holunda.polyflow.view.Task
import io.holunda.polyflow.view.query.FilterQuery

/**
 * Query for task of a certain process application.
 * @param applicationName  the name of the process application.
 */
data class TasksForApplicationQuery(val applicationName: String) : FilterQuery<Task> {
  override fun applyFilter(element: Task): Boolean = element.sourceReference.applicationName == applicationName
}
