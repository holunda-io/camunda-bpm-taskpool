package io.holunda.polyflow.view.query.process.variable.filter

import io.holunda.polyflow.view.ProcessVariable
import io.holunda.polyflow.view.query.process.variable.ProcessVariableFilter
import io.holunda.polyflow.view.query.process.variable.ProcessVariableFilterType

/**
 * Filter to query for exactly one variable.
 */
data class ProcessVariableFilterExactlyOne(
  val processVariableName: String
) : ProcessVariableFilter {

  override val type: ProcessVariableFilterType = ProcessVariableFilterType.INCLUDE
  override fun apply(variable: ProcessVariable): Boolean = processVariableName == variable.variableName
}
