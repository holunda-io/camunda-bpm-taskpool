package io.holunda.camunda.taskpool.view.query.process

import io.holunda.camunda.taskpool.view.ProcessInstance
import io.holunda.camunda.taskpool.view.query.PageableSortableQuery
import io.holunda.camunda.taskpool.view.query.QueryResult

/**
 * Result for process instance queries.
 */
data class ProcessInstanceQueryResult(
  override val elements: List<ProcessInstance>
) : QueryResult<ProcessInstance, ProcessInstanceQueryResult>(elements = elements) {
  override fun slice(query: PageableSortableQuery) = this.copy(elements = super.slice(query).elements)
}
