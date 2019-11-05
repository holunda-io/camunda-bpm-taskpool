package io.holunda.camunda.taskpool.view.query.task

import io.holunda.camunda.taskpool.view.Task
import io.holunda.camunda.taskpool.view.query.FilterQuery

/**
 * Query for task of a certain process application.
 * @param applicationName  the name of the process application.
 */
data class TasksForApplicationQuery(val applicationName: String) : FilterQuery<Task> {
  override fun applyFilter(element: Task): Boolean = element.sourceReference.applicationName == applicationName
}
