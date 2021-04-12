package io.holunda.camunda.taskpool.view.query.process.variable

import io.holunda.camunda.taskpool.view.ProcessVariable
import java.util.function.Function

/**
 * Process variable filter
 */
interface ProcessVariableFilter : Function<ProcessVariable, Boolean> {
  val type: ProcessVariableFilterType
}

/**
 * Filter type.
 */
enum class ProcessVariableFilterType {
  /**
   * Include variables.
   */
  INCLUDE,

  /**
   * Exclude variables.
   */
  EXCLUDE
}


