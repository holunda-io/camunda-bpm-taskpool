package io.holunda.camunda.taskpool.view.query.process

import io.holunda.camunda.taskpool.view.ProcessInstance
import io.holunda.camunda.taskpool.view.ProcessInstanceState
import io.holunda.camunda.taskpool.view.query.FilterQuery

/**
 * Query for process instance matching the provided states.
 */
data class ProcessInstancesByStateQuery(
  val states: Set<ProcessInstanceState>
) : FilterQuery<ProcessInstance> {
  override fun applyFilter(element: ProcessInstance): Boolean = states.contains(element = element.state)
}
