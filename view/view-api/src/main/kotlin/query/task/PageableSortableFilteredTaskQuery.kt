package io.holunda.polyflow.view.query.task

import io.holunda.polyflow.view.Task
import io.holunda.polyflow.view.query.FilterQuery
import io.holunda.polyflow.view.query.PageableSortableQuery

/**
 * Pageable and sortable task query containing filters.
 */
interface PageableSortableFilteredTaskQuery : FilterQuery<Task>, PageableSortableQuery {
  val filters: List<String>
}
