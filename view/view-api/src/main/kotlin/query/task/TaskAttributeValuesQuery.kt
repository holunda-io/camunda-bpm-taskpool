package io.holunda.polyflow.view.query.task

import io.holunda.polyflow.view.Task
import io.holunda.polyflow.view.auth.User
import io.holunda.polyflow.view.query.FilterQuery
import io.holunda.polyflow.view.query.PageableSortableQuery

/**
 * Query and distinct tasks attribute values for the given attribute name.
 * @param user - the user with groups accessing the tasks. If non is passed, all tasks attribute values will be queried.
 * @param assignedToMeOnly flag indicating if the resulting tasks must be assigned to the user only.
 * @param filters - currently not supported
 */
data class TaskAttributeValuesQuery(
  val attributeName: String,
  val user: User?,
  val assignedToMeOnly: Boolean = false,
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

