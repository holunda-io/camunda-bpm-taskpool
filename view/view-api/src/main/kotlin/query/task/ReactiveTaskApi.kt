package io.holunda.polyflow.view.query.task

import io.holunda.polyflow.view.Task
import io.holunda.polyflow.view.TaskWithDataEntries
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * Reactive API to retrieve tasks.
 * @see TaskApi
 * For the client, there is no difference in definition of the query, but the implementer has a different method to reflect the reactive nature.
 */
interface ReactiveTaskApi {

  /**
   * Queries user tasks with data.
   */
  fun query(query: TasksWithDataEntriesForUserQuery): CompletableFuture<TasksWithDataEntriesQueryResult>

  /**
   * Queries user tasks for task id.
   */
  fun query(query: TaskWithDataEntriesForIdQuery): CompletableFuture<Optional<TaskWithDataEntries>>

  /**
   * Count user tasks for applications.
   */
  fun query(query: TaskCountByApplicationQuery): CompletableFuture<List<ApplicationWithTaskCount>>

  /**
   * Tasks for user.
   */
  fun query(query: TasksForUserQuery): CompletableFuture<TaskQueryResult>

  /**
   * Task for id.
   */
  fun query(query: TaskForIdQuery): CompletableFuture<Optional<Task>>

  /**
   * Query tasks for a given process application.
   */
  fun query(query: TasksForApplicationQuery): CompletableFuture<TaskQueryResult>

}
