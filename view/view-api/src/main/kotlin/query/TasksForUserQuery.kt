package io.holunda.camunda.taskpool.view.query

import io.holunda.camunda.taskpool.view.Task
import io.holunda.camunda.taskpool.view.auth.User

data class TasksForUserQuery(
  val user: User
) : FilterQuery<Task> {

  override fun applyFilter(element: Task) =
  // assignee
    element.assignee == this.user.username
      // candidate user
      || (element.candidateUsers.contains(this.user.username))
      // candidate groups:
      || (element.candidateGroups.any { candidateGroup -> this.user.groups.contains(candidateGroup) })
}

data class TaskList(val tasks: List<Task>)

sealed class TaskChangeEvent(open val task: Task)
data class TaskCreate(override val task: Task) : TaskChangeEvent(task)
data class TaskDelete(override val task: Task) : TaskChangeEvent(task)
data class TaskChange(override val task: Task) : TaskChangeEvent(task)




