package io.holunda.camunda.taskpool.view.query.process.variable.filter

import io.holunda.camunda.taskpool.view.ProcessVariable
import io.holunda.camunda.taskpool.view.query.process.variable.ProcessVariableFilter
import io.holunda.camunda.taskpool.view.query.process.variable.ProcessVariableFilterType

data class ProcessVariableFilterExactlyOne(
  val processVariableName: String
) : ProcessVariableFilter {

  override val type: ProcessVariableFilterType = ProcessVariableFilterType.INCLUDE
  override fun apply(variable: ProcessVariable): Boolean = processVariableName == variable.variableName
}
