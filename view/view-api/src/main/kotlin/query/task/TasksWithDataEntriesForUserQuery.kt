package io.holunda.polyflow.view.query.task

import io.holunda.polyflow.view.TaskWithDataEntries
import io.holunda.polyflow.view.auth.User
import io.holunda.polyflow.view.query.FilterQuery
import io.holunda.polyflow.view.query.PageableSortableQuery

/**
 * Query for tasks with correlated data entries for given user.
 * @param user user able to see the tasks.
 * @param page current page.
 * @param size number of entries on every page.
 * @param sort property name of the {@link TaskWithDataEntries} to sort.
 * @param filters list of filters
 */
data class TasksWithDataEntriesForUserQuery(
  val user: User,
  override val page: Int = 1,
  override val size: Int = Int.MAX_VALUE,
  override val sort: String? = null,
  val filters: List<String> = listOf()
) : FilterQuery<TaskWithDataEntries>, PageableSortableQuery {

  override fun applyFilter(element: TaskWithDataEntries): Boolean = element.task.assignee == user.username
      // candidate user
      || (element.task.candidateUsers.contains(user.username))
      // candidate groups
      || (element.task.candidateGroups.any { candidateGroup -> user.groups.contains(candidateGroup) })

}
