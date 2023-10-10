package io.holunda.polyflow.view.query.task

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
  override val sort: List<String> = listOf(),
  val filters: List<String> = listOf()
) : FilterQuery<TaskWithDataEntries>, PageableSortableQuery {

  @Deprecated("Please use other constructor setting sort as List<String>")
  constructor(user: User, includeAssigned: Boolean = false, page: Int = 0, size: Int = Int.MAX_VALUE, sort: String, filters: List<String> = listOf()) : this(
    user = user, includeAssigned = includeAssigned, page = page, size = size, if (sort.isBlank()) listOf() else listOf(sort), filters = filters
  )

  override fun applyFilter(element: TaskWithDataEntries): Boolean =
    TasksForGroupQuery(user, includeAssigned, page, size, sort, filters).applyFilter(element.task)
}
