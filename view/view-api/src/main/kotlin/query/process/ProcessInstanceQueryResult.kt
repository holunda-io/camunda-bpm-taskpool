package io.holunda.polyflow.view.query.process

import io.holunda.polyflow.view.ProcessInstance
import io.holunda.polyflow.view.query.PageableSortableQuery
import io.holunda.polyflow.view.query.QueryResult

/**
 * Result for process instance queries.
 */
data class ProcessInstanceQueryResult(
  override val elements: List<ProcessInstance>
) : QueryResult<ProcessInstance, ProcessInstanceQueryResult>(elements = elements) {
  override fun slice(query: PageableSortableQuery) = this.copy(elements = super.slice(query).elements)
}
