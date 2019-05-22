package io.holunda.camunda.taskpool.view.query

import io.holunda.camunda.taskpool.view.Task
import io.holunda.camunda.taskpool.view.TaskWithDataEntries
import io.holunda.camunda.taskpool.view.query.task.*

interface TaskApi {

  fun query(query: TasksWithDataEntriesForUserQuery): TasksWithDataEntriesQueryResult

  fun query(query: TaskWithDataEntriesForIdQuery): TaskWithDataEntries?

  fun query(query: TaskCountByApplicationQuery): List<ApplicationWithTaskCount>

  fun query(query: TasksForUserQuery): TaskQueryResult

  fun query(query: TaskForIdQuery): Task?

}
