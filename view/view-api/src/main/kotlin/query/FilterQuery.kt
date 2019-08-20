package io.holunda.camunda.taskpool.view.query

/**
 * Query using a filter function.
 */
interface FilterQuery<T : Any> {
  fun applyFilter(element: T): Boolean
}
