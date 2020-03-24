package io.holunda.camunda.taskpool.view.query

/**
 * Query using a filter function.
 */
interface FilterQuery<T : Any> {
  /**
   * Applies the filter.
   * @param element element to check.
   * @return true, if the element is included into the result.
   */
  fun applyFilter(element: T): Boolean
}
