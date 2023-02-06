package io.holunda.polyflow.view.query.task

import io.holunda.polyflow.view.Task
import io.holunda.polyflow.view.TaskWithDataEntries
import io.holunda.polyflow.view.auth.User
import io.holunda.polyflow.view.query.FilterQuery
import io.holunda.polyflow.view.query.PageableSortableQuery

/**
 * Query for tasks visible for user based on the candidate group membership.
 * @param user - the user with groups accessing the tasks.
 * @param includeAssigned flag indicating if the assigned tasks should be included or not.
 * @param page - page to read, zero-based index.
 * @param size - size of the page
 * @param sort - optional attribute to sort by.
 * @param filters - the filters to further filter down the tasks.
 */
data class TasksWithDataEntriesForGroupQuery(
  val user: User,
  val includeAssigned: Boolean = false,
  override val page: Int = 0,
  override val size: Int = Int.MAX_VALUE,
  override val sort: String? = null,
  val filters: List<String> = listOf()
) : FilterQuery<TaskWithDataEntries>, PageableSortableQuery {

  override fun applyFilter(element: TaskWithDataEntries): Boolean =
    TasksForGroupQuery(user, includeAssigned, page, size, sort, filters).applyFilter(element.task)
}
