package io.holunda.camunda.taskpool.view.query

class TaskCountByApplicationQuery

data class ApplicationWithTaskCount(
  val application: String,
  val taskCount: Int
)
