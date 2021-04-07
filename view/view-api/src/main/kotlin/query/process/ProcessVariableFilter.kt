package io.holunda.camunda.taskpool.view.query.process

import io.holunda.camunda.taskpool.view.ProcessVariable
import java.util.function.Function

/**
 * Process variable filter
 */
interface ProcessVariableFilter: Function<ProcessVariable, Boolean> {
  val type: ProcessVariableFilterType
}

enum class ProcessVariableFilterType {
  INCLUDE,
  EXCLUDE
}
