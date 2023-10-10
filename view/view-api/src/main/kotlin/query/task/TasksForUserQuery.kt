package io.holunda.polyflow.view.query.task

import io.holunda.polyflow.view.Task
import io.holunda.polyflow.view.auth.User

/**
 * Query for tasks visible for user.
 * @param user - the user with groups accessing the tasks.
 * @param assignedToMeOnly flag indicating if the resulting tasks must be assigned to the user only.
 * @param page - page to read, zero-based index.
 * @param size - size of the page
 * @param sort - optional attribute to sort by.
 * @param filters - the filters to further filter down the tasks.
 */
data class TasksForUserQuery(
  val user: User,
  val assignedToMeOnly: Boolean = false,
  override val page: Int = 0,
  override val size: Int = Int.MAX_VALUE,
  override val sort: List<String> = listOf(),
  override val filters: List<String> = listOf()
) : PageableSortableFilteredTaskQuery {

  @Deprecated("Please use other constructor setting sort as List<String>")
  constructor(user: User, assignedToMeOnly: Boolean = false, page: Int = 0, size: Int = Int.MAX_VALUE, sort: String, filters: List<String> = listOf()): this(
    user = user,
    assignedToMeOnly = assignedToMeOnly,
    page = page,
    size = size,
    sort = if (sort.isBlank()) listOf() else listOf(sort),
    filters = filters
  )

  /**
   * Compatibility constructor for old clients.
   */
  @Deprecated(message = "Please use other constructor setting the assignedToMeOnly.")
  constructor(user: User, page: Int = 0, size: Int = Int.MAX_VALUE, sort: String? = null, filters: List<String> = listOf()): this(
    user = user,
    assignedToMeOnly = false,
    page = page,
    size = size,
    sort = if (sort.isNullOrBlank()) listOf() else listOf(sort),
    filters = filters
  )

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

