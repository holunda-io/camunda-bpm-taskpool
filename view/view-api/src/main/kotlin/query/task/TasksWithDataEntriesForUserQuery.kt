package io.holunda.polyflow.view.query.task

import io.holunda.polyflow.view.TaskWithDataEntries
import io.holunda.polyflow.view.auth.User
import io.holunda.polyflow.view.query.FilterQuery
import io.holunda.polyflow.view.query.PageableSortableQuery

/**
 * Query for tasks with correlated data entries for given user.
 * @param user user able to see the tasks.
 * @param includeAssigned flag indicating if assigned tasks should be returned.
 * @param assignedToMeOnly flag indicating if the resulting tasks must be assigned to the user only.
 * @param page current page, zero-based index.
 * @param size number of entries on every page.
 * @param sort property name of the {@link TaskWithDataEntries} to sort.
 * @param filters list of filters
 */
data class TasksWithDataEntriesForUserQuery(
  val user: User,
  val assignedToMeOnly: Boolean = false,
  override val page: Int = 0,
  override val size: Int = Int.MAX_VALUE,
  override val sort: String? = null,
  val filters: List<String> = listOf()
) : FilterQuery<TaskWithDataEntries>, PageableSortableQuery {

  override fun applyFilter(element: TaskWithDataEntries): Boolean =
    TasksForUserQuery(
      user = user,
      assignedToMeOnly = assignedToMeOnly,
      page = page,
      size = size,
      sort = sort,
      filters = filters
    ).applyFilter(element.task)
}
