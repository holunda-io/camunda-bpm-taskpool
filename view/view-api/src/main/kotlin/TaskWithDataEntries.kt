package io.holunda.camunda.taskpool.view

data class TaskWithDataEntries(
  val task: Task,
  val dataEntries: List<DataEntry> = listOf()
) {
  companion object {
    fun correlate(task: Task, dataEntries: List<DataEntry>): TaskWithDataEntries =
      TaskWithDataEntries(
        task = task,
        dataEntries = dataEntries.filter { entry ->
          // task correlation list contains entryType -> entryId elements
          // create data entry identity with it and take only data entries with this identity
          task.correlationIdentities.contains(entry.identity)
        }.toList()
      )

    fun correlate(tasks: List<Task>, dataEntry: DataEntry): List<TaskWithDataEntries> =
      tasks.filter { it.correlationIdentities.contains(dataEntry.identity) }
        .map { TaskWithDataEntries(it, listOf(dataEntry)) }
        .toList()

    fun correlate(tasks: List<Task>, dataEntries: List<DataEntry>) =
      tasks.map { correlate(it, dataEntries) }
  }
}
