package io.holunda.polyflow.view.query.task

import io.holunda.polyflow.view.query.PageableSortableQuery
import io.holunda.polyflow.view.query.QueryResult

/**
 * Result of query for multiple task attributes.
 */
data class TaskAttributeNamesQueryResult(
  override val elements: List<String>,
  override val totalElementCount: Int = elements.size
) : QueryResult<String, TaskAttributeNamesQueryResult>(elements = elements, totalElementCount = totalElementCount) {
  override fun slice(query: PageableSortableQuery) = this.copy(elements = super.slice(query).elements)
}
