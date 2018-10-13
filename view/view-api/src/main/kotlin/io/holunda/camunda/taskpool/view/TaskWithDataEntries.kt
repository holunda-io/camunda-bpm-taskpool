package io.holunda.camunda.taskpool.view

import io.holunda.camunda.taskpool.api.business.EntryId
import io.holunda.camunda.taskpool.api.business.dataIdentity

data class TasksWithDataEntries(
  val task: Task,
  val dataEntries: List<DataEntry>
)

fun tasksWithDataEntries(task: Task, dataEntries: Map<String, DataEntry>) =
  TasksWithDataEntries(
    task = task,
    dataEntries = dataEntries.filter { entry ->
      // task correlation list contains entryType -> entryId elements
      // create data entry identity with it and tak only data entries with this identity
      task.correlations.map { dataIdentity(it.key, it.value as EntryId) }.contains(entry.key)
    }.values.toList()
  )


