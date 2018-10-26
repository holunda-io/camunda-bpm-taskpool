package io.holunda.camunda.taskpool.view.query

import io.holunda.camunda.taskpool.view.TaskWithDataEntries

data class TasksWithDataEntriesResponse(
  val elementCount: Int,
  val tasksWithDataEntries: List<TaskWithDataEntries>
)
