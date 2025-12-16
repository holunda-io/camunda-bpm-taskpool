package io.holunda.polyflow.view.query.task

import io.holunda.polyflow.view.Task
import io.holunda.polyflow.view.auth.User

/**
 * Query for tasks visible for user.
 * @param user - the user with groups accessing the tasks.
 * @param assignedToMeOnly flag indicating if the resulting tasks must be assigned to the user only.
 * @param involvementsOnly flag indicating if the results should only contain tasks where the user is involved in any of the correlated data entries
 * @param page - page to read, zero-based index.
 * @param size - size of the page
 * @param sort - optional attribute to sort by.
 * @param filters - the filters to further filter down the tasks.
 */
data class TasksForUserQuery(
  val user: User,
  val assignedToMeOnly: Boolean = false,
  val involvementsOnly: Boolean = false,
  override val page: Int = 0,
  override val size: Int = Int.MAX_VALUE,
  override val sort: List<String> = listOf(),
  override val filters: List<String> = listOf()
) : PageableSortableFilteredTaskQuery {

//  @Deprecated("Please use other constructor setting the involvementsOnly flag")
//  constructor(user: User, assignedToMeOnly: Boolean = false, page: Int = 0, size: Int = Int.MAX_VALUE, sort: List<String> = listOf(), filters: List<String> = listOf()): this(
//    user = user,
//    assignedToMeOnly = assignedToMeOnly,
//    involvementsOnly = false,
//    page = page,
//    size = size,
//    sort = sort,
//    filters = filters
//  )

  override fun applyFilter(element: Task): Boolean =
    if (assignedToMeOnly) {
      // assignee
      element.assignee == this.user.username
    } else {
      // candidate user
      element.candidateUsers.contains(this.user.username)
        // candidate groups
        || element.candidateGroups.any { candidateGroup -> this.user.groups.contains(candidateGroup) }
        // assignee
        || element.assignee == this.user.username
    }
}

