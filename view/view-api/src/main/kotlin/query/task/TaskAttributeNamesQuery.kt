package io.holunda.polyflow.view.query.task

import io.holunda.polyflow.view.Task
import io.holunda.polyflow.view.auth.User
import io.holunda.polyflow.view.query.FilterQuery

/**
 * Query for tasks attribute names.
 * @param user - the user with groups accessing the tasks. If non is passed, all task attributes will be queried.
 * @param assignedToMeOnly flag indicating if the resulting tasks must be assigned to the user only.
 */
data class TaskAttributeNamesQuery(
  val user: User?,
  val assignedToMeOnly: Boolean = false,
) : FilterQuery<Task> {

  override fun applyFilter(element: Task): Boolean = true

}

