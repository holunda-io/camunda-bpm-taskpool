package io.holunda.polyflow.view.query.process.variable

import io.holunda.polyflow.view.ProcessVariable

/**
 * Response of the process variable query.
 */
data class ProcessVariableQueryResult(
  val variables: List<ProcessVariable>
)
