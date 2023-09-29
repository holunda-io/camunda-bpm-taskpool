package io.holunda.polyflow.view.query

import io.holunda.polyflow.view.sort.SortDirection.ASCENDING
import io.holunda.polyflow.view.sort.SortDirection.DESCENDING
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties

/**
 * Query interface for queries specifying the total amount of results, page and sort.
 */
interface PageableSortableQuery {
  val page: Int
  val size: Int
  val sort: List<String>

  /**
   * Checks that the sort parameter is correctly specified.
   * @param clazz class containing member properties to be used as allowed fields.
   */
  fun <C : KClass<*>> sanitizeSort(clazz: C) {
    sanitizeSort(clazz.declaredMemberProperties.map { it.name }.toSet())
  }

  /**
   * Checks that the sort parameter is correctly specified.
   */
  fun sanitizeSort(fieldNames: Set<String>) {
    if (sort.isEmpty()) {
      return
    }
    sort.forEach {

      val direction = it!!.substring(0, 1)
      require(
        direction == ASCENDING.sign
          || direction == DESCENDING.sign
      ) { "Sort must start either with '${ASCENDING.sign}' or '${DESCENDING.sign}' but it was starting with '$direction'" }
      val parameter = it!!.substring(1)
      require(fieldNames.contains(parameter)) {
        "Sort parameter must be one of ${
          fieldNames.joinToString(", ")
        } but it was $parameter."
      }
    }
  }

}
