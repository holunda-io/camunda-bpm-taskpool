package io.holunda.camunda.taskpool.view.query.process

import io.holunda.camunda.taskpool.view.ProcessVariable

/**
 * Response of the process variable query.
 */
data class ProcessVariableQueryResult(
  val variables: List<ProcessVariable>
)
