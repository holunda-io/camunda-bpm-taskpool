package io.holunda.camunda.taskpool.view.query

import io.holunda.camunda.taskpool.view.TaskWithDataEntries
import io.holunda.camunda.taskpool.view.auth.User

/**
 * Query for tasks with correlated data entries for given user.
 * @param user user able to see the tasks.
 * @param page current page.
 * @param size number of entries on every page.
 * @param sort property name of the {@link TaskWithDataEntries} to sort.
 * @param filters list of filters
 */
data class TasksWithDataEntriesForUserQuery(
  val user: User,
  val page: Int,
  val size: Int,
  val sort: String? = null,
  val filters: List<String> = listOf(),
  val filterMethod: (TaskWithDataEntries) -> Boolean = { true }
) : FilterQuery<TaskWithDataEntries> {

  override fun applyFilter(element: TaskWithDataEntries): Boolean = filterMethod(element)
}
