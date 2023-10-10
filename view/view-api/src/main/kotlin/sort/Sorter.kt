package io.holunda.polyflow.view.sort

import io.holunda.polyflow.view.DataEntry
import io.holunda.polyflow.view.Task
import io.holunda.polyflow.view.TaskWithDataEntries
import io.holunda.polyflow.view.filter.*
import io.holunda.polyflow.view.filter.isDataEntryAttribute
import io.holunda.polyflow.view.filter.isTaskAttribute
import java.lang.reflect.Field
import java.time.Instant
import java.util.*
import kotlin.Comparator

/**
 * Creates a new data entry comparator.
 * @param sort a sort string (like +field or -name)
 * @return comparator for the sort string.
 */
internal fun dataComparator(sort: String?): DataEntryComparator? {
  if (sort.isNullOrBlank()) {
    return null
  }
  val sortDirection = parse(sort) ?: return null
  val fieldName = if (isDataEntryAttribute(sort.substring(1))) {
    sort.substring(1).substring(DATA_PREFIX.length)
  } else {
    return null
  }
  val field = extractField(targetClass = DataEntry::class.java, name = fieldName) ?: return null
  return DataEntryComparator(field to sortDirection)
}

/**
 * Creates a new data entry comparator.
 * @param sort a list of sort strings (like +field or -name)
 * @return comparator for the sort strings.
 */
fun dataComparator(sort: List<String>): Comparator<DataEntry>? {
  return mapComparator(sort.map { dataComparator(it) })
}

/**
 * Creates a new task comparator.
 * @param sort a sort string (like +field or -name)
 * @return comparator for the sort string.
 */
internal fun taskComparator(sort: String?): TaskComparator? {
  if (sort.isNullOrBlank()) {
    return null
  }
  val sortDirection = parse(sort) ?: return null
  val fieldName = if (isTaskAttribute(sort.substring(1))) {
    sort.substring(1).substring(TASK_PREFIX.length)
  } else {
    return null
  }
  val field = extractField(targetClass = Task::class.java, name = fieldName)
    ?: return null
  return TaskComparator(field to sortDirection)
}

/**
 * Creates a new task comparator.
 * @param sort a list of sort strings (like +field or -name)
 * @return comparator for the sort strings.
 */
fun taskComparator(sort: List<String>): Comparator<Task>? {
  if (sort.isEmpty()) {
    return null
  }
  return mapComparator(sort.map { taskComparator(it) })
}

/**
 * Creates a new task with data entries comparator.
 * @param sort a sort string (like +field or -name)
 * @return comparator for the sort string.
 */
internal fun taskWithDataEntriesComparator(sort: String?): TasksWithDataEntriesComparator? {
  if (sort.isNullOrBlank()) {
    return null
  }
  val sortDirection = parse(sort) ?: return null
  val fieldName = if (isTaskAttribute(sort.substring(1))) {
    sort.substring(1).substring(TASK_PREFIX.length)
  } else if (isDataEntryAttribute(sort.substring(1))) {
    sort.substring(1).substring(DATA_PREFIX.length)
  } else {
    return null
  }
  val field = extractField(targetClass = Task::class.java, name = fieldName)
    ?: return null
  return TasksWithDataEntriesComparator(field to sortDirection)
}

/**
 * Creates a new task with data entries comparator.
 * @param sort a list of sort strings (like +field or -name)
 * @return comparator for the sort strings.
 */
fun taskWithDataEntriesComparator(sort: List<String>): Comparator<TaskWithDataEntries>? {
  if (sort.isEmpty()) {
    return null
  };
  return mapComparator(sort.map { taskWithDataEntriesComparator(it) });
}

private fun <T> mapComparator(sort: List<Comparator<T>?>): Comparator<T>? {
  return sort.filterNotNull().reduceOrNull { combined, element -> combined.then(element) }
}

/**
 * Comparison mode.
 */
enum class CompareMode {
  EQUAL, LESS_THAN, GREATER_THAN, DEFAULT
}

/**
 * Sort direction.
 */
enum class SortDirection(val modifier: Int, val sign: String) {
  ASCENDING(1, "+"),
  DESCENDING(-1, "-");
}

/**
 * Parses sort direction.
 */
internal fun parse(sort: String) = SortDirection.values().find { it.sign == sort.substring(0, 1) }

/**
 * Finds comparison mode.
 */
internal fun findCompareMode(o1: Any?, o2: Any?): CompareMode {
  return when {
    (null != o1) and (null != o2) -> CompareMode.DEFAULT
    (null == o1) and (null != o2) -> CompareMode.LESS_THAN
    (null != o1) and (null == o2) -> CompareMode.GREATER_THAN
    (null == o1) and (null == o2) -> CompareMode.EQUAL
    else -> CompareMode.LESS_THAN
  }
}

/**
 * Compares.
 */
internal fun compareActual(fieldSort: Pair<Field, SortDirection>, v1: Any?, v2: Any?): Int {
  return when (fieldSort.first.type) {
    java.lang.Integer::class.java -> (v1 as Int).compareTo(v2 as Int) * fieldSort.second.modifier
    java.lang.String::class.java -> (v1 as String).compareTo(v2 as String) * fieldSort.second.modifier
    java.util.Date::class.java -> (v1 as Date).compareTo(v2 as Date) * fieldSort.second.modifier
    Instant::class.java -> (v1 as Instant).compareTo(v2 as Instant) * fieldSort.second.modifier
    Int::class.java -> (v1 as Int).compareTo(v2 as Int) * fieldSort.second.modifier
    else -> throw UnsupportedOperationException("Unknown type ${fieldSort.first.type}")
  }
}


