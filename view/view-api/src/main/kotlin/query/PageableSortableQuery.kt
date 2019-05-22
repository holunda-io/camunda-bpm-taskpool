package io.holunda.camunda.taskpool.view.query

/**
 * Query interface for queries specifying the total amount of results, page and sort.
 */
interface PageableSortableQuery {
  val page: Int
  val size: Int
  val sort: String?
}
