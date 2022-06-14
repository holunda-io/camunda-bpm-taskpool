package io.holunda.polyflow.view

import io.holunda.polyflow.view.query.task.*
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import java.util.concurrent.CompletableFuture

/**
 * Client encapsulating the correct query types (including response types)
 */
class TaskQueryClient(
  private val queryGateway: QueryGateway
) {

  /**
   * @see io.holunda.polyflow.view.query.task.TaskApi.query
   * @see io.holunda.polyflow.view.query.task.TasksForUserQuery
   */
  fun query(query: TasksForUserQuery): CompletableFuture<TaskQueryResult> =
    queryGateway.query(
      query,
      ResponseTypes.instanceOf(TaskQueryResult::class.java)
    )

  /**
   * @see io.holunda.polyflow.view.query.task.TaskApi.query
   * @see io.holunda.polyflow.view.query.task.TaskForIdQuery
   */
  fun query(query: TaskForIdQuery): CompletableFuture<Task?> = queryGateway.query(
    query,
    ResponseTypes.instanceOf(Task::class.java)
  )

  /**
   * @see io.holunda.polyflow.view.query.task.TaskApi.query
   * @see io.holunda.polyflow.view.query.task.TasksForApplicationQuery
   */
  fun query(query: TasksForApplicationQuery): CompletableFuture<TaskQueryResult> = queryGateway.query(
    query,
    ResponseTypes.instanceOf(TaskQueryResult::class.java)
  )

  /**
   * @see io.holunda.polyflow.view.query.task.TaskApi.query
   * @see io.holunda.polyflow.view.query.task.TaskWithDataEntriesForIdQuery
   */
  fun query(query: TaskWithDataEntriesForIdQuery): CompletableFuture<TaskWithDataEntries?> = queryGateway.query(
    query,
    ResponseTypes.instanceOf(TaskWithDataEntries::class.java)
  )

  /**
   * @see io.holunda.polyflow.view.query.task.TaskApi.query
   * @see io.holunda.polyflow.view.query.task.TasksWithDataEntriesForUserQuery
   */
  fun query(query: TasksWithDataEntriesForUserQuery): CompletableFuture<TasksWithDataEntriesQueryResult> = queryGateway.query(
    query,
    ResponseTypes.instanceOf(TasksWithDataEntriesQueryResult::class.java)
  )

  /**
   * @see io.holunda.polyflow.view.query.task.TaskApi.query
   * @see io.holunda.polyflow.view.query.task.TaskCountByApplicationQuery
   */
  fun query(query: TaskCountByApplicationQuery): CompletableFuture<List<ApplicationWithTaskCount>> = queryGateway.query(
    query,
    ResponseTypes.multipleInstancesOf(ApplicationWithTaskCount::class.java)
  )

}
