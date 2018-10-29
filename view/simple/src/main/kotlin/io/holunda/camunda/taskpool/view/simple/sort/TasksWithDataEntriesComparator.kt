package io.holunda.camunda.taskpool.view.simple.sort

import io.holunda.camunda.taskpool.view.TaskWithDataEntries
import java.lang.reflect.Field

data class TasksWithDataEntriesComparator(
  private val fields: Map<Field, SortDirection>
) : Comparator<TaskWithDataEntries> {

  constructor(sortInstructions: Set<String>): this(createFields(sortInstructions))

  override fun compare(o1: TaskWithDataEntries, o2: TaskWithDataEntries): Int {
    return 0
  }
}

private fun createFields(sortInstructions: Set<String>): Map<Field, SortDirection> {
  return mapOf()
}

enum class SortDirection(val modifier: Int) {
  ASCENDING(1),
  DESCENDING(-1)
}
