package io.holunda.polyflow.view.query.task

import io.holunda.polyflow.view.Task
import io.holunda.polyflow.view.auth.User
import io.holunda.polyflow.view.query.FilterQuery
import io.holunda.polyflow.view.query.PageableSortableQuery

/**
 * Query for tasks attribute names.
 * @param user - the user with groups accessing the tasks. If non is passed, all task attributes will be queried.
 * @param assignedToMeOnly flag indicating if the resulting tasks must be assigned to the user only.
 * @param filters - the filters to further filter down the task attributes.
 */
data class TaskAttributeNamesQuery(
  val user: User?,
  val assignedToMeOnly: Boolean = false,
  val filters: List<String> = listOf(),
) : FilterQuery<Task> {

  override fun applyFilter(element: Task): Boolean = user == null ||
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

