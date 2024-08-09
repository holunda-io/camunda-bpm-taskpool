package io.holunda.polyflow.view

import io.holunda.polyflow.view.query.task.*
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * Client encapsulating the correct query types (including response types)
 */
open class TaskQueryClient(
  private val queryGateway: QueryGateway
) {

  /**
   * @see io.holunda.polyflow.view.query.task.TaskApi.query
   * @see io.holunda.polyflow.view.query.task.TasksForUserQuery
   */
  open fun query(query: TasksForUserQuery): CompletableFuture<TaskQueryResult> =
    queryGateway.query(
      query,
      ResponseTypes.instanceOf(TaskQueryResult::class.java)
    )

  /**
   * @see io.holunda.polyflow.view.query.task.TaskApi.query
   * @see io.holunda.polyflow.view.query.task.TaskForIdQuery
   */
  open fun query(query: TaskForIdQuery): CompletableFuture<Optional<Task>> = queryGateway.query(
    query,
    ResponseTypes.optionalInstanceOf(Task::class.java)
  )

  /**
   * @see io.holunda.polyflow.view.query.task.TaskApi.query
   * @see io.holunda.polyflow.view.query.task.TasksForApplicationQuery
   */
  open fun query(query: TasksForApplicationQuery): CompletableFuture<TaskQueryResult> = queryGateway.query(
    query,
    ResponseTypes.instanceOf(TaskQueryResult::class.java)
  )

  /**
   * @see io.holunda.polyflow.view.query.task.TaskApi.query
   * @see io.holunda.polyflow.view.query.task.TaskWithDataEntriesForIdQuery
   */
  open fun query(query: TaskWithDataEntriesForIdQuery): CompletableFuture<Optional<TaskWithDataEntries>> = queryGateway.query(
    query,
    ResponseTypes.optionalInstanceOf(TaskWithDataEntries::class.java)
  )

  /**
   * @see io.holunda.polyflow.view.query.task.TaskApi.query
   * @see io.holunda.polyflow.view.query.task.TasksWithDataEntriesForUserQuery
   */
  open fun query(query: TasksWithDataEntriesForUserQuery): CompletableFuture<TasksWithDataEntriesQueryResult> = queryGateway.query(
    query,
    ResponseTypes.instanceOf(TasksWithDataEntriesQueryResult::class.java)
  )

  /**
   * @see io.holunda.polyflow.view.query.task.TaskApi.query
   * @see io.holunda.polyflow.view.query.task.TaskCountByApplicationQuery
   */
  open fun query(query: TaskCountByApplicationQuery): CompletableFuture<List<ApplicationWithTaskCount>> = queryGateway.query(
    query,
    ResponseTypes.multipleInstancesOf(ApplicationWithTaskCount::class.java)
  )

  /**
   * @see io.holunda.polyflow.view.query.task.TaskApi.query
   * @see io.holunda.polyflow.view.query.task.AllTasksQuery
   */
  open fun query(query: AllTasksQuery): CompletableFuture<TaskQueryResult> = queryGateway.query(
    query,
    ResponseTypes.instanceOf(TaskQueryResult::class.java)
  )

  /**
   * @see io.holunda.polyflow.view.query.task.TaskApi.query
   * @see io.holunda.polyflow.view.query.task.TasksWithDataEntriesQueryResult
   */
  open fun query(query: AllTasksWithDataEntriesQuery): CompletableFuture<TasksWithDataEntriesQueryResult> = queryGateway.query(
    query,
    ResponseTypes.instanceOf(TasksWithDataEntriesQueryResult::class.java)
  )

  /**
   * @see io.holunda.polyflow.view.query.task.TaskApi.query
   * @see io.holunda.polyflow.view.query.task.TasksForGroupQuery
   */
  open fun query(query: TasksForGroupQuery): CompletableFuture<TaskQueryResult> = queryGateway.query(
    query,
    ResponseTypes.instanceOf(TaskQueryResult::class.java)
  )

  /**
   * @see io.holunda.polyflow.view.query.task.TaskApi.query
   * @see io.holunda.polyflow.view.query.task.TasksWithDataEntriesForGroupQuery
   */
  open fun query(query: TasksWithDataEntriesForGroupQuery): CompletableFuture<TasksWithDataEntriesQueryResult> = queryGateway.query(
    query,
    ResponseTypes.instanceOf(TasksWithDataEntriesQueryResult::class.java)
  )

  /**
   * @see io.holunda.polyflow.view.query.task.TaskApi.query
   * @see io.holunda.polyflow.view.query.task.TasksForCandidateUserAndGroupQuery
   */
  open fun query(query: TasksForCandidateUserAndGroupQuery): CompletableFuture<TaskQueryResult> = queryGateway.query(
    query,
    ResponseTypes.instanceOf(TaskQueryResult::class.java)
  )

  /**
   * @see io.holunda.polyflow.view.query.task.TaskApi.query
   * @see io.holunda.polyflow.view.query.task.TaskAttributeNamesQuery
   */
  open fun query(query: TaskAttributeNamesQuery): CompletableFuture<TaskAttributeNamesQueryResult> = queryGateway.query(
    query,
    ResponseTypes.instanceOf(TaskAttributeNamesQueryResult::class.java)
  )

  /**
   * @see io.holunda.polyflow.view.query.task.TaskApi.query
   * @see io.holunda.polyflow.view.query.task.TaskAttributeValuesQuery
   */
  open fun query(query: TaskAttributeValuesQuery): CompletableFuture<TaskAttributeValuesQueryResult> = queryGateway.query(
    query,
    ResponseTypes.instanceOf(TaskAttributeValuesQueryResult::class.java)
  )

}
