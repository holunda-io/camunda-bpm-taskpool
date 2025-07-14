package io.holunda.polyflow.view.query.task

import io.holunda.polyflow.view.query.PageableSortableQuery
import io.holunda.polyflow.view.query.QueryResult

/**
 * Result of query for multiple task attributes.
 */
data class TaskAttributeValuesQueryResult(
  override val elements: List<Any>,
  override val totalElementCount: Int = elements.size
) : QueryResult<Any, TaskAttributeValuesQueryResult>(elements = elements, totalElementCount = totalElementCount) {
  override fun slice(query: PageableSortableQuery) = this.copy(elements = super.slice(query).elements)
}
