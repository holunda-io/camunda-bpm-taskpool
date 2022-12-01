package io.holunda.polyflow.view.query.task

import io.holunda.polyflow.view.Task
import io.holunda.polyflow.view.auth.User
import io.holunda.polyflow.view.query.FilterQuery
import io.holunda.polyflow.view.query.PageableSortableQuery

/**
 * Query for tasks visible for user.
 * @param user - the user with groups accessing the tasks.
 * @param page - page to read.
 * @param size - size of the page
 * @param sort - optional attribute to sort by.
 * @param filters - the filters to further filter down the tasks.
 */
data class TasksForUserQuery(
  val user: User,
  override val page: Int = 1,
  override val size: Int = Int.MAX_VALUE,
  override val sort: String? = null,
  val filters: List<String> = listOf()
) : FilterQuery<Task>, PageableSortableQuery {

  override fun applyFilter(element: Task): Boolean =
  // assignee
    element.assignee == this.user.username
      // candidate user
      || (element.candidateUsers.contains(this.user.username))
      // candidate groups
      || (element.candidateGroups.any { candidateGroup -> this.user.groups.contains(candidateGroup) })
}

