package io.holunda.camunda.taskpool.example.tasklist.rest.filter

import io.holunda.camunda.taskpool.view.Task
import io.holunda.camunda.taskpool.view.TasksWithDataEntries
import org.springframework.util.ReflectionUtils
import java.lang.reflect.Field
import java.util.function.Predicate
import kotlin.reflect.full.memberProperties

const val SEPARATOR = ".-."

fun filter(filters: List<String>, values: List<TasksWithDataEntries>): List<TasksWithDataEntries> {
  val criteria = toCriteria(filters)
  val predicates = createPredicates(criteria)
  return filterByPredicates(values, predicates.first, predicates.second)
}

private fun toCriteria(filters: List<String>) = filters
  .asSequence()
  .filter { it.contains(SEPARATOR) }
  .map {
    val components = it.split(SEPARATOR)
    if (isTaskAttribute(components[0])) {
      TaskCriterium(components[0], components[1])
    } else {
      DataEntryCriterium(components[0], components[1])
    }
  }.toList()

private fun isTaskAttribute(propertyName: String): Boolean = Task::class.memberProperties.map { it.name }.contains(propertyName)

private fun createPredicates(criteria: List<Criterium>): Pair<List<Predicate<Any>>, List<Predicate<Any>>> {
  val taskPredicates = criteria.asSequence().filter { it is TaskCriterium }.map { PropertyValuePredicate(name = it.name, value = it.value) }.toList()
  val dataEntriesPredicates = criteria.asSequence().filter { it is DataEntryCriterium }.map { PropertyValuePredicate(name = it.name, value = it.value) }.toList()
  return taskPredicates to dataEntriesPredicates
}

fun filterByPredicates(values: List<TasksWithDataEntries>, taskPredicates: List<Predicate<Any>>, dataEntriesPredicates: List<Predicate<Any>>): List<TasksWithDataEntries> {

  val taskPredicate = if (taskPredicates.isEmpty()) {
    null
  } else {
    taskPredicates.reduce { combined, predicate -> combined.and(predicate) }
  }
  val dataEntriesPredicate = if (dataEntriesPredicates.isEmpty()) {
    null
  } else {
    dataEntriesPredicates.reduce{combined, predicate -> combined.and(predicate)}
  }

  return values.filter {
    // no constraints
    (taskPredicate == null && dataEntriesPredicate == null)
    // constraint is defined on task and matches on task property
    (taskPredicate != null && taskPredicate.test(it.task))
      // constraint is defined on data and matches on data entry property
      || (dataEntriesPredicate != null && it.dataEntries.find{ dataEntry: Any -> dataEntriesPredicate.test(dataEntry) } != null)
  }
}

sealed class Criterium(open val name: String, open val value: String)
class TaskCriterium(override val name: String, override val value: String) : Criterium(name, value)
class DataEntryCriterium(override val name: String, override val value: String) : Criterium(name, value)


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
        value.toString() == valueExtractor(target, field)
      } catch (e: IllegalStateException) {
        false
      }
    } else {
      !ignoreMissing
    }
  }
}

fun extractField(target: Any, name: String): Field? = ReflectionUtils.findField(target::class.java, name)
fun extractValue(target: Any, field: Field): Any? = ReflectionUtils.getField(field, target)


