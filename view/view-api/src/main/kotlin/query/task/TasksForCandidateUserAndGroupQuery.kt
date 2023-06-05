package io.holunda.polyflow.view.query.task

import io.holunda.polyflow.view.Task
import io.holunda.polyflow.view.auth.User

/**
 * Query for tasks visible for user.
 * @param user - the user with groups accessing the tasks.
 * @param includeAssigned flag indicating if assigned tasks are returned.
 * @param page - page to read, zero-based index.
 * @param size - size of the page
 * @param sort - optional attribute to sort by.
 * @param filters - the filters to further filter down the tasks.
 */
data class TasksForCandidateUserAndGroupQuery(
  val user: User,
  val includeAssigned: Boolean = true,
  override val page: Int = 0,
  override val size: Int = Int.MAX_VALUE,
  override val sort: String? = null,
  override val filters: List<String> = listOf()
) : PageableSortableFilteredTaskQuery {

  override fun applyFilter(element: Task): Boolean =
    (element.candidateGroups.any { candidateGroup -> this.user.groups.contains(candidateGroup) }
      || element.candidateUsers.contains(this.user.username)) && ((element.assignee != null) == includeAssigned)

}

