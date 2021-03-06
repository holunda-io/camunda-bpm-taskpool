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
    fun correlate(task: Task, dataEntries: Map<String, DataEntry>): TaskWithDataEntries =
      TaskWithDataEntries(
        task = task,
        dataEntries = task.correlationIdentities.mapNotNull { dataEntries[it] }
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
    fun correlate(tasks: List<Task>, dataEntries: Map<String, DataEntry>): List<TaskWithDataEntries> =
      tasks.map { correlate(it, dataEntries) }
  }
}
