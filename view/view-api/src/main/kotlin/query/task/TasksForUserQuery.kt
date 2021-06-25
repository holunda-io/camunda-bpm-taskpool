package io.holunda.polyflow.view.query.task

import io.holunda.polyflow.view.Task
import io.holunda.polyflow.view.auth.User
import io.holunda.polyflow.view.query.FilterQuery

/**
 * Query for tasks visible for user.
 * @param user - the user with groups accessing the tasks.
 */
data class TasksForUserQuery(
  val user: User
) : FilterQuery<Task> {

  override fun applyFilter(element: Task): Boolean =
  // assignee
    element.assignee == this.user.username
      // candidate user
      || (element.candidateUsers.contains(this.user.username))
      // candidate groups
      || (element.candidateGroups.any { candidateGroup -> this.user.groups.contains(candidateGroup) })
}

