package io.holunda.camunda.taskpool.view.mongo.service

import io.holunda.camunda.taskpool.view.Task
import kotlin.reflect.full.memberProperties

const val EQUALS = "="
const val GREATER = ">"
const val LESS = "<"
const val TASK_PREFIX = "task."
val OPERATORS = Regex("[$EQUALS$LESS$GREATER]")

/**
 * Forms criteria from string filters.
 */
fun toCriteria(filters: List<String>) = filters.map { toCriterion(it) }.filter { it !is Criterion.EmptyCriterion }

/**
 * Forms a single criteria from string filter.
 */
internal fun toCriterion(filter: String): Criterion {

  require(filter.isNotBlank()) { "Failed to create criteria from empty filter '$filter'." }

  if (!filter.contains(OPERATORS)) {
    return Criterion.EmptyCriterion
  }

  val segments = when {
    filter.contains(EQUALS) -> filter.split(EQUALS).plus(EQUALS)
    filter.contains(GREATER) -> filter.split(GREATER).plus(GREATER)
    filter.contains(LESS) -> filter.split(LESS).plus(LESS)
    else -> listOf()
  }
  require(segments.size == 3 && !segments[0].isBlank() && !segments[1].isBlank()) { "Failed to create criteria from $filter." }

  return if (isTaskAttribute(segments[0])) {
    Criterion.TaskCriterion(name = segments[0].substring(TASK_PREFIX.length), value = segments[1], operator = segments[2])
  } else {
    Criterion.DataEntryCriterion(name = segments[0], value = segments[1], operator = segments[2])
  }
}


/**
 * Checks is a property is a task attribute.
 */
internal fun isTaskAttribute(propertyName: String): Boolean =
  propertyName.startsWith(TASK_PREFIX)
    && propertyName.length > TASK_PREFIX.length
    && Task::class.memberProperties.map { it.name }.contains(propertyName.substring(TASK_PREFIX.length))

/**
 * Criterion.
 */
sealed class Criterion(open val name: String, open val value: String, open val operator: String) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is Criterion) return false

    if (name != other.name) return false
    if (value != other.value) return false
    if (operator != other.operator) return false

    return true
  }

  override fun hashCode(): Int {
    var result = name.hashCode()
    result = 31 * result + value.hashCode()
    result = 31 * result + operator.hashCode()
    return result
  }

  object EmptyCriterion : Criterion("empty", "no value", "none")
  /**
   * Criterion on task.
   */
  data class TaskCriterion(override val name: String, override val value: String, override val operator: String = EQUALS) : Criterion(name, value, operator)

  /**
   * Criterion on data entry.
   */
  data class DataEntryCriterion(override val name: String, override val value: String, override val operator: String = EQUALS) : Criterion(name, value, operator)
}
