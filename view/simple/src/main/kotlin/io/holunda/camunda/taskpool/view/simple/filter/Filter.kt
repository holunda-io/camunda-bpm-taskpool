package io.holunda.camunda.taskpool.view.simple.filter

import io.holunda.camunda.taskpool.view.Task
import io.holunda.camunda.taskpool.view.TaskWithDataEntries
import org.springframework.util.ReflectionUtils
import java.lang.reflect.Field
import java.math.BigDecimal
import java.time.Instant
import java.util.*
import java.util.function.Predicate
import kotlin.reflect.full.memberProperties

const val EQUALS = "="
const val GREATER = ">"
const val LESS = "<"
const val TASK_PREFIX = "task."

/**
 * Comparator function.
 */
typealias CompareOperator = (Any, Any?) -> Boolean

val OPERATORS = Regex("[$EQUALS$LESS$GREATER]")

/**
 * Implemented comparison support for some data types.
 */
internal fun compareOperator(sign: String): CompareOperator =
  when (sign) {
    LESS -> { filter, actual ->
      when (actual) {
        is String -> actual.endsWith(filter.toString())
        is Int -> actual < Integer.parseInt(filter.toString())
        is Date -> actual.toInstant().isBefore(Instant.parse(filter.toString()))
        is BigDecimal -> actual.subtract(BigDecimal(filter.toString())) < BigDecimal.ZERO
        else -> actual.toString().endsWith(filter.toString())
      }
    }
    GREATER -> { filter, actual ->
      when (actual) {
        is String -> actual.startsWith(filter.toString())
        is Int -> actual > Integer.parseInt(filter.toString())
        is Date -> actual.toInstant().isAfter(Instant.parse(filter.toString()))
        is BigDecimal -> actual.subtract(BigDecimal(filter.toString())) > BigDecimal.ZERO
        else -> actual.toString().startsWith(filter.toString())
      }
    }
    EQUALS -> { filter, actual -> filter.toString() == actual.toString() }
    else -> throw IllegalArgumentException("Unsupported operator $sign")
  }

/**
 * Filters the list of tasks by provided filters.
 */
internal fun filter(filters: List<String>, values: List<TaskWithDataEntries>): List<TaskWithDataEntries> {
  val predicates = createPredicates(toCriteria(filters))
  return filterByPredicates(values, predicates)
}

/**
 * Filters by applying applies predicates on the list of tasks.
 */
internal fun filterByPredicates(values: List<TaskWithDataEntries>, wrapper: TaskPredicateWrapper): List<TaskWithDataEntries> = values.filter { filterByPredicates(it, wrapper) }


/**
 * Checks if a single task matches the predicates.
 */
internal fun filterByPredicates(value: TaskWithDataEntries, wrapper: TaskPredicateWrapper): Boolean =
// no constraints
  (wrapper.taskPredicate == null && wrapper.dataEntriesPredicate == null)
    // constraint is defined on task and matches on task property
    || (wrapper.taskPredicate != null && wrapper.taskPredicate.test(value.task))
    // constraint is defined on data and matches on data entry property
    || (wrapper.dataEntriesPredicate != null
    && (value.dataEntries
    .asSequence()
    .map { dataEntry -> dataEntry.payload }
    .find { payload -> wrapper.dataEntriesPredicate.test(payload) } != null || wrapper.dataEntriesPredicate.test(value.task.payload)))

/**
 * Constructs predicates out of criteria.
 */
internal fun createPredicates(criteria: List<Criterion>): TaskPredicateWrapper {
  val taskPredicates: List<Predicate<Any>> = criteria
    .asSequence()
    .filter { it is Criterion.TaskCriterion }
    .map {
      PropertyValuePredicate(
        name = it.name,
        value = it.value,
        // extract field here
        fieldExtractor = { target, fieldName -> extractField(target, fieldName) },
        valueExtractor = { target, field -> extractValue(target, field) },
        compareOperator = compareOperator(it.operator)
      )
    }
    .toList()

  val dataEntriesPredicates: List<Predicate<Any>> = criteria
    .asSequence()
    .filter { it is Criterion.DataEntryCriterion }
    .map {
      PropertyValuePredicate(
        name = it.name,
        value = it.value,
        // extract key from the map
        fieldExtractor = { target, fieldName -> extractKey(target, fieldName) },
        valueExtractor = { target, key -> extractValue(target, key) },
        compareOperator = compareOperator(it.operator)
      )
    }
    .toList()

  val taskPredicate = if (taskPredicates.isEmpty()) {
    null
  } else {
    taskPredicates.reduce { combined, predicate -> combined.or(predicate) }
  }
  val dataEntriesPredicate = if (dataEntriesPredicates.isEmpty()) {
    null
  } else {
    dataEntriesPredicates.reduce { combined, predicate -> combined.or(predicate) }
  }

  return TaskPredicateWrapper(taskPredicate, dataEntriesPredicate)
}

/**
 * Forms criteria from string filters.
 */
internal fun toCriteria(filters: List<String>) = filters.map { toCriterion(it) }.filter { it !is Criterion.EmptyCriterion }

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
  require(segments.size == 3 && !segments[0].isBlank() && !segments[0].isBlank()) { "Failed to create criteria from $filter." }

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

  /**
   * Empty aka null-objects.
   */
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

/**
 * Wrapper for a pair of task and data entry predicate.
 */
data class TaskPredicateWrapper(val taskPredicate: Predicate<Any>?, val dataEntriesPredicate: Predicate<Any>?)

/**
 * <V> type of the property
 */
data class PropertyValuePredicate<T>(
  private val name: String,
  private val value: Any,
  private val fieldExtractor: (Any, String) -> T?,
  private val valueExtractor: (Any, T) -> Any?,
  private val ignoreMissing: Boolean = true,
  private val compareOperator: CompareOperator
) : Predicate<Any> {

  override fun test(target: Any): Boolean {
    val field = fieldExtractor(target, name)
    return if (field != null) {
      try {
        val extracted = valueExtractor(target, field)
        compareOperator(value, extracted)
      } catch (e: IllegalStateException) {
        false
      }
    } else {
      !ignoreMissing
    }
  }
}

/**
 * Retrieves a filed from class.
 */
fun extractField(targetClass: Class<*>, name: String): Field? = ReflectionUtils.findField(targetClass, name)

/**
 * Retrieves a field from object.
 */
fun extractField(target: Any, name: String): Field? = extractField(target::class.java, name)

/**
 * Extracts value from object usgin field.
 */
fun extractValue(target: Any, field: Field): Any? = ReflectionUtils.getField(field.apply { ReflectionUtils.makeAccessible(this) }, target)

/**
 * Extract key from map or [null]
 */
fun extractKey(target: Any, name: String): String? = if (target is Map<*, *> && target.containsKey(name)) name else null

/**
 * Extracts value from map or [null]
 */
fun extractValue(target: Any, key: String): Any? = if (target is Map<*, *>) target[key] else null

