package io.holunda.camunda.taskpool.view.query

import io.holunda.camunda.taskpool.view.TaskWithDataEntries
import io.holunda.camunda.taskpool.view.auth.User
import java.util.*

/**
 * Query for tasks with correlated data entries for given user.
 */
data class TasksDataEntryForUserQuery(
  val user: User,
  val page: Optional<Int> = Optional.empty(),
  val size: Optional<Int> = Optional.empty(),
  val sort: List<String> = listOf(),
  val filters: List<String> = listOf(),
  val filterMethod: (TaskWithDataEntries) -> Boolean = { true }
) : FilterQuery<TaskWithDataEntries> {

  override fun applyFilter(element: TaskWithDataEntries): Boolean = filterMethod(element)
}
