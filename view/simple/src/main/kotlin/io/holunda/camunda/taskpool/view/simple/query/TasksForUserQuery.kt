package io.holunda.camunda.taskpool.view.simple.query

import io.holunda.camunda.taskpool.view.simple.service.Task
import io.holunda.camunda.taskpool.view.simple.service.User
import org.axonframework.queryhandling.QueryGateway
import org.axonframework.queryhandling.responsetypes.ResponseTypes

data class TasksForUserQuery(
  val user: User
) : FilterQuery<Task> {

  fun subscribeTo(queryGateway: QueryGateway) = with(queryGateway.subscriptionQuery(
    this,
    ResponseTypes.instanceOf(TaskList::class.java),
    ResponseTypes.multipleInstancesOf(TaskChangeEvent::class.java))) {
    // executed after subscription is created
    initialResult()
  }

  override fun applyFilter(task: Task) =
// assignee
    task.assignee == this.user.username
      // candidate user
      || (task.candidateUsers.contains(this.user.username))
      // candidate groups:
      || (task.candidateGroups.any { candidateGroup -> this.user.groups.contains(candidateGroup) })


}

data class TaskList(val tasks: List<Task>)

sealed class TaskChangeEvent(open val task: Task)
data class TaskCreate(override val task: Task) : TaskChangeEvent(task)
data class TaskDelete(override val task: Task) : TaskChangeEvent(task)
data class TaskChange(override val task: Task) : TaskChangeEvent(task)




