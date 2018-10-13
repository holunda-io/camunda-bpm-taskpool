package io.holunda.camunda.taskpool.example.tasklist.rest.filter

import io.holunda.camunda.taskpool.view.Task
import io.holunda.camunda.taskpool.view.TasksWithDataEntries
import org.springframework.util.ReflectionUtils
import java.lang.reflect.Field
import java.util.function.Predicate
import kotlin.reflect.full.memberProperties

const val SEPARATOR = "="

fun filter(filters: List<String>, values: List<TasksWithDataEntries>): List<TasksWithDataEntries> {
  val predicates = createPredicates(toCriteria(filters))
  return filterByPredicates(values, predicates)
}

internal fun toCriteria(filters: List<String>) = filters
  .asSequence()
  .filter { it.contains(SEPARATOR) }
  .map {
    val components = it.split(SEPARATOR)
    if (components.size != 2 || components[0].isBlank() || components[0].isBlank()) throw IllegalArgumentException("Failed to create criteria from $it.")
    if (isTaskAttribute(components[0])) {
      TaskCriterium(components[0], components[1])
    } else {
      DataEntryCriterium(components[0], components[1])
    }
  }.toList()

internal fun isTaskAttribute(propertyName: String): Boolean = Task::class.memberProperties.map { it.name }.contains(propertyName)

internal fun createPredicates(criteria: List<Criterium>): TaskPredicateWrapper {
  val taskPredicates: List<Predicate<Any>> = criteria.asSequence().filter { it is TaskCriterium }.map { PropertyValuePredicate(name = it.name, value = it.value) }.toList()
  val dataEntriesPredicates: List<Predicate<Any>> = criteria.asSequence().filter { it is DataEntryCriterium }.map { PropertyValuePredicate(name = it.name, value = it.value) }.toList()

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

internal fun filterByPredicates(values: List<TasksWithDataEntries>, wrapper: TaskPredicateWrapper): List<TasksWithDataEntries> {

  return values.filter {
    // no constraints
    (wrapper.taskPredicate == null && wrapper.dataEntriesPredicate == null)
      // constraint is defined on task and matches on task property
      || (wrapper.taskPredicate != null && wrapper.taskPredicate.test(it.task))
      // constraint is defined on data and matches on data entry property
      || (wrapper.dataEntriesPredicate != null
      && it.dataEntries
      .asSequence()
      .map { dataEntry -> dataEntry.payload }
      .find { payload -> wrapper.dataEntriesPredicate.test(payload) } != null)
  }
}

sealed class Criterium(open val name: String, open val value: String) {
  override fun equals(other: Any?): Boolean {

    if (this === other) return true
    if (other !is Criterium) return false

    if (name != other.name) return false
    if (value != other.value) return false

    return true
  }

  override fun hashCode(): Int {
    var result = name.hashCode()
    result = 31 * result + value.hashCode()
    return result
  }
}

class TaskCriterium(override val name: String, override val value: String) : Criterium(name, value)
class DataEntryCriterium(override val name: String, override val value: String) : Criterium(name, value)

data class TaskPredicateWrapper(val taskPredicate: Predicate<Any>?, val dataEntriesPredicate: Predicate<Any>?)

/**
 * <V> type of the property
 */
data class PropertyValuePredicate(
  private val name: String,
  private val value: Any,
  private val fieldExtractor: (Any, String) -> Field? = { t, fieldName -> extractField(t, fieldName) },
  private val valueExtractor: (Any, Field) -> Any? = { target, field -> extractValue(target, field) },
  private val ignoreMissing: Boolean = true
) : Predicate<Any> {

  override fun test(target: Any): Boolean {
    val field = fieldExtractor(target, name)
    return if (field != null) {
      try {
        val extracted = valueExtractor(target, field)
        value.toString() == extracted
      } catch (e: IllegalStateException) {
        false
      }
    } else {
      !ignoreMissing
    }
  }
}

fun extractField(target: Any, name: String): Field? = ReflectionUtils.findField(target::class.java, name)
fun extractValue(target: Any, field: Field): Any? = ReflectionUtils.getField(field.apply { ReflectionUtils.makeAccessible(this) }, target)



