package io.holunda.camunda.taskpool.view.query.task

import io.holunda.camunda.taskpool.view.Task
import io.holunda.camunda.taskpool.view.auth.User
import io.holunda.camunda.taskpool.view.query.FilterQuery

/**
 * Query for tasks visible for user.
 * @param user - the user with groups accessing the tasks.
 */
data class TasksForUserQuery(
  val user: User
) : FilterQuery<Task> {

  override fun applyFilter(element: Task) =
  // assignee
    element.assignee == this.user.username
      // candidate user
      || (element.candidateUsers.contains(this.user.username))
      // candidate groups
      || (element.candidateGroups.any { candidateGroup -> this.user.groups.contains(candidateGroup) })
}

