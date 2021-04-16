package io.holunda.camunda.taskpool.view.query.process.variable.filter

import io.holunda.camunda.taskpool.view.ProcessVariable
import io.holunda.camunda.taskpool.view.query.process.variable.ProcessVariableFilter
import io.holunda.camunda.taskpool.view.query.process.variable.ProcessVariableFilterType

/**
 * Filter to query for a list of variables. Every variable present in the list matches the filter.
 */
data class ProcessVariableOneOf(
  val processVariableNames: Set<String>
) : ProcessVariableFilter {

  init {
    require(processVariableNames.isNotEmpty()) { "You must specify at least one variable for this filter." }
  }

  override val type: ProcessVariableFilterType = ProcessVariableFilterType.INCLUDE
  override fun apply(variable: ProcessVariable): Boolean = processVariableNames.contains(variable.variableName)
}
