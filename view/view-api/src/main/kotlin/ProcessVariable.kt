package io.holunda.camunda.taskpool.view

import io.holunda.camunda.taskpool.api.process.variable.ProcessVariableValue
import io.holunda.camunda.taskpool.api.task.SourceReference

/**
 * Represents a process variable.
 */
data class ProcessVariable(
  val variableName: String,
  val variableInstanceId: String,
  val sourceReference: SourceReference,
  val scopeActivityInstanceId: String,
  val value: ProcessVariableValue
)
