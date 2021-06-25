package io.holunda.polyflow.view.query.task

import io.holunda.polyflow.view.Task
import io.holunda.polyflow.view.TaskWithDataEntries
import java.util.concurrent.CompletableFuture

/**
 * Reactive API to retrieve tasks.
 */
interface ReactiveTaskApi {

  /**
   * Queries user tasks with data.
   */
  fun query(query: TasksWithDataEntriesForUserQuery): CompletableFuture<TasksWithDataEntriesQueryResult>

  /**
   * Queries user tasks for task id.
   */
  fun query(query: TaskWithDataEntriesForIdQuery): CompletableFuture<TaskWithDataEntries?>

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
  fun query(query: TaskForIdQuery): CompletableFuture<Task?>

  /**
   * Query tasks for a given process application.
   */
  fun query(query: TasksForApplicationQuery): CompletableFuture<TaskQueryResult>

}
