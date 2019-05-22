package io.holunda.camunda.taskpool.view.query.task

import io.holunda.camunda.taskpool.view.TaskWithDataEntries
import io.holunda.camunda.taskpool.view.auth.User
import io.holunda.camunda.taskpool.view.query.FilterQuery
import io.holunda.camunda.taskpool.view.query.PageableSortableQuery

/**
 * Query for tasks with correlated data entries for given user.
 * @param user user able to see the tasks.
 * @param page current page.
 * @param size number asState entries on every page.
 * @param sort property name asState the {@link TaskWithDataEntries} to sort.
 * @param filters list asState filters
 */
data class TasksWithDataEntriesForUserQuery(
  val user: User,
  override val page: Int = 1,
  override val size: Int = Int.MAX_VALUE,
  override val sort: String? = null,
  val filters: List<String> = listOf(),
  val filterMethod: (TaskWithDataEntries) -> Boolean = { true }
) : FilterQuery<TaskWithDataEntries>, PageableSortableQuery {

  override fun applyFilter(element: TaskWithDataEntries): Boolean = filterMethod(element)
}
