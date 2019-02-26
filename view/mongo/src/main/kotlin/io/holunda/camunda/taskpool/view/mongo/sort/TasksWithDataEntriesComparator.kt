package io.holunda.camunda.taskpool.view.mongo.sort

import io.holunda.camunda.taskpool.view.Task
import io.holunda.camunda.taskpool.view.TaskWithDataEntries
import io.holunda.camunda.taskpool.view.mongo.filter.TASK_PREFIX
import io.holunda.camunda.taskpool.view.mongo.filter.extractField
import io.holunda.camunda.taskpool.view.mongo.filter.extractValue
import io.holunda.camunda.taskpool.view.mongo.filter.isTaskAttribute
import java.lang.reflect.Field
import java.util.*
import javax.xml.datatype.DatatypeConstants.LESSER

private enum class CompareMode {
  EQUAL, LESS_THAN, GREATER_THAN, DEFAULT
}

data class TasksWithDataEntriesComparator(
  private val fieldSort: Pair<Field, SortDirection>
) : Comparator<TaskWithDataEntries> {
  override fun compare(o1: TaskWithDataEntries, o2: TaskWithDataEntries): Int {
    return try {

      val v1 = extractValue(o1.task, this.fieldSort.first)
      val v2 = extractValue(o2.task, this.fieldSort.first)

      when (findCompareMode(v1, v2)) {
        CompareMode.DEFAULT -> compareActual(v1, v2)
        CompareMode.LESS_THAN -> -1 * this.fieldSort.second.modifier
        CompareMode.GREATER_THAN -> 1 * this.fieldSort.second.modifier
        CompareMode.EQUAL -> 0 * this.fieldSort.second.modifier
      }
    } catch (e: Exception) {
      LESSER
    }
  }

  private fun findCompareMode(o1: Any?, o2: Any?): CompareMode {
    return when {
      (null != o1) and (null != o2) -> CompareMode.DEFAULT
      (null == o1) and (null != o2) -> CompareMode.LESS_THAN
      (null != o1) and (null == o2) -> CompareMode.GREATER_THAN
      (null == o1) and (null == o2) -> CompareMode.EQUAL
      else -> CompareMode.LESS_THAN
    }
  }

  private fun compareActual(v1: Any?, v2: Any?): Int {
    return when (this.fieldSort.first.type) {
      java.lang.Integer::class.java -> (v1 as Int).compareTo(v2 as Int) * this.fieldSort.second.modifier
      java.lang.String::class.java -> (v1 as String).compareTo(v2 as String) * this.fieldSort.second.modifier
      java.util.Date::class.java -> (v1 as Date).compareTo(v2 as Date) * this.fieldSort.second.modifier
      Int::class.java -> (v1 as Int).compareTo(v2 as Int) * this.fieldSort.second.modifier
      else -> throw UnsupportedOperationException("Unknown type ${this.fieldSort.first.type}")
    }
  }

}


fun comparator(sort: String?): TasksWithDataEntriesComparator? {
  if (sort == null || sort.isBlank()) return null
  val sortDirection = parse(sort) ?: return null
  val fieldName = if (isTaskAttribute(sort.substring(1))) {
    sort.substring(1).substring(TASK_PREFIX.length)
  } else {
    return null
  }
  val field = extractField(targetClass = Task::class.java, name = fieldName)
    ?: return null
  return TasksWithDataEntriesComparator(field to sortDirection)
}


enum class SortDirection(val modifier: Int, val sign: String) {
  ASCENDING(1, "+"),
  DESCENDING(-1, "-");
}

internal fun parse(sort: String) = SortDirection.values().find { it.sign == sort.substring(0, 1) }
