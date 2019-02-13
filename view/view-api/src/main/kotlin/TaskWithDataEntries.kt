package io.holunda.camunda.taskpool.view

data class TaskWithDataEntries(
  val task: Task,
  val dataEntries: List<DataEntry> = listOf()
)


