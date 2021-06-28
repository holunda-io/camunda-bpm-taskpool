package io.holunda.polyflow.view.query.process

import io.holunda.polyflow.view.ProcessInstance
import io.holunda.polyflow.view.ProcessInstanceState
import io.holunda.polyflow.view.query.FilterQuery

/**
 * Query for process instance matching the provided states.
 */
data class ProcessInstancesByStateQuery(
  val states: Set<ProcessInstanceState>
) : FilterQuery<ProcessInstance> {
  override fun applyFilter(element: ProcessInstance): Boolean = states.contains(element = element.state)
}
