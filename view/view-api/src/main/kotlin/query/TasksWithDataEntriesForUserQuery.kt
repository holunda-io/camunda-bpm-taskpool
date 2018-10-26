package io.holunda.camunda.taskpool.view.query

import io.holunda.camunda.taskpool.view.TaskWithDataEntries
import io.holunda.camunda.taskpool.view.auth.User

/**
 * Query for tasks with correlated data entries for given user.
 */
data class TasksWithDataEntriesForUserQuery(
  val user: User,
  val page: Int,
  val size: Int,
  val sort: List<String> = listOf(),
  val filters: List<String> = listOf(),
  val filterMethod: (TaskWithDataEntries) -> Boolean = { true }
) : FilterQuery<TaskWithDataEntries> {

  override fun applyFilter(element: TaskWithDataEntries): Boolean = filterMethod(element)
}
