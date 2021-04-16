package io.holunda.camunda.taskpool.view.query.process.variable

import io.holunda.camunda.taskpool.view.ProcessVariable
import io.holunda.camunda.taskpool.view.query.FilterQuery

/**
 * Query for variables of given process instance.
 * @param processInstanceId process instance id to query for.
 * @param variableFilter
 */
data class ProcessVariablesForInstanceQuery(
  val processInstanceId: String,
  val variableFilter: List<ProcessVariableFilter>
) : FilterQuery<ProcessVariable> {

  override fun applyFilter(element: ProcessVariable): Boolean =
    element.sourceReference.instanceId == processInstanceId
      && (variableFilter.isEmpty() || variableFilter.any { filter -> filter.apply(element) })

}
