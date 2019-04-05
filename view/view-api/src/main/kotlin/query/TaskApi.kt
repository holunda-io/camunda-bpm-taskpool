package io.holunda.camunda.taskpool.view.query

import io.holunda.camunda.taskpool.view.DataEntry
import io.holunda.camunda.taskpool.view.Task
import io.holunda.camunda.taskpool.view.TaskWithDataEntries

interface TaskApi {

  fun query(query: TasksWithDataEntriesForUserQuery): TasksWithDataEntriesResponse

  fun query(query: TaskCountByApplicationQuery): List<ApplicationWithTaskCount>

  fun query(query: TaskForIdQuery): Task?

  fun query(query: TaskWithDataEntriesForIdQuery): TaskWithDataEntries?

  fun query(query: DataEntryQuery): List<DataEntry>

  fun query(query: TasksForUserQuery): List<Task>
}
