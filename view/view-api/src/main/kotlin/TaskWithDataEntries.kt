package io.holunda.polyflow.view

/**
 * Represents a joined entity of a task with correlated data entries.
 */
data class TaskWithDataEntries(
  /**
   * Task from the engine.
   */
  val task: Task,
  /**
   * List of data entries, bound using task correlation ids.
   */
  val dataEntries: List<DataEntry> = listOf()
) {
  companion object {
    /**
     * Helper function to correlate.
     */
    fun correlate(task: Task, dataEntries: List<DataEntry>): TaskWithDataEntries =
      TaskWithDataEntries(
        task = task,
        dataEntries = dataEntries.filter { entry ->
          // task correlation list contains entryType -> entryId elements
          // create data entry identity with it and take only data entries with this identity
          task.correlationIdentities.contains(entry.identity)
        }.toList()
      )

    /**
     * Helper function to correlate.
     */
    fun correlate(tasks: List<Task>, dataEntry: DataEntry): List<TaskWithDataEntries> =
      tasks.filter { it.correlationIdentities.contains(dataEntry.identity) }
        .map { TaskWithDataEntries(it, listOf(dataEntry)) }
        .toList()

    /**
     * Helper function to correlate.
     */
    fun correlate(tasks: List<Task>, dataEntries: List<DataEntry>) =
      tasks.map { correlate(it, dataEntries) }
  }
}
