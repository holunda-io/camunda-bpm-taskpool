package io.holunda.camunda.taskpool.view.query

import io.holunda.camunda.taskpool.view.Task
import io.holunda.camunda.taskpool.view.TaskWithDataEntries
import io.holunda.camunda.taskpool.view.query.task.*
import java.util.concurrent.CompletableFuture

interface ReactiveTaskApi {

  fun query(query: TasksWithDataEntriesForUserQuery): CompletableFuture<TasksWithDataEntriesQueryResult>

  fun query(query: TaskWithDataEntriesForIdQuery): CompletableFuture<TaskWithDataEntries?>

  fun query(query: TaskCountByApplicationQuery): CompletableFuture<List<ApplicationWithTaskCount>>

  fun query(query: TasksForUserQuery): CompletableFuture<TaskQueryResult>

  fun query(query: TaskForIdQuery): CompletableFuture<Task?>

}
