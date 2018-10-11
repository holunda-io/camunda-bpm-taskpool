package io.holunda.camunda.taskpool.view.simple.query

interface FilterQuery<T: Any> {
  fun applyFilter(element: T): Boolean
}
