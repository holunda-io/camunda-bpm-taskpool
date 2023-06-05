package io.holunda.polyflow.view

import io.holunda.polyflow.view.query.task.*
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import java.util.*
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
  fun query(query: TaskForIdQuery): CompletableFuture<Optional<Task>> = queryGateway.query(
    query,
    ResponseTypes.optionalInstanceOf(Task::class.java)
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
  fun query(query: TaskWithDataEntriesForIdQuery): CompletableFuture<Optional<TaskWithDataEntries>> = queryGateway.query(
    query,
    ResponseTypes.optionalInstanceOf(TaskWithDataEntries::class.java)
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

  /**
   * @see io.holunda.polyflow.view.query.task.TaskApi.query
   * @see io.holunda.polyflow.view.query.task.AllTasksQuery
   */
  fun query(query: AllTasksQuery): CompletableFuture<TaskQueryResult> = queryGateway.query(
    query,
    ResponseTypes.instanceOf(TaskQueryResult::class.java)
  )

  /**
   * @see io.holunda.polyflow.view.query.task.TaskApi.query
   * @see io.holunda.polyflow.view.query.task.TasksWithDataEntriesQueryResult
   */
  fun query(query: AllTasksWithDataEntriesQuery): CompletableFuture<TasksWithDataEntriesQueryResult> = queryGateway.query(
    query,
    ResponseTypes.instanceOf(TasksWithDataEntriesQueryResult::class.java)
  )

  /**
   * @see io.holunda.polyflow.view.query.task.TaskApi.query
   * @see io.holunda.polyflow.view.query.task.TasksForGroupQuery
   */
  fun query(query: TasksForGroupQuery): CompletableFuture<TaskQueryResult> = queryGateway.query(
    query,
    ResponseTypes.instanceOf(TaskQueryResult::class.java)
  )

  /**
   * @see io.holunda.polyflow.view.query.task.TaskApi.query
   * @see io.holunda.polyflow.view.query.task.TasksWithDataEntriesForGroupQuery
   */
  fun query(query: TasksWithDataEntriesForGroupQuery): CompletableFuture<TasksWithDataEntriesQueryResult> = queryGateway.query(
    query,
    ResponseTypes.instanceOf(TasksWithDataEntriesQueryResult::class.java)
  )

  /**
   * @see io.holunda.polyflow.view.query.task.TaskApi.query
   * @see io.holunda.polyflow.view.query.task.TasksForCandidateUserAndGroupQuery
   */
  fun query(query: TasksForCandidateUserAndGroupQuery): CompletableFuture<TaskQueryResult> = queryGateway.query(
    query,
    ResponseTypes.instanceOf(TaskQueryResult::class.java)
  )

}
