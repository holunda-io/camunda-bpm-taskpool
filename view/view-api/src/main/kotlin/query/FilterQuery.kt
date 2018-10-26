package io.holunda.camunda.taskpool.view.query

interface FilterQuery<T: Any> {
  fun applyFilter(element: T): Boolean
}
