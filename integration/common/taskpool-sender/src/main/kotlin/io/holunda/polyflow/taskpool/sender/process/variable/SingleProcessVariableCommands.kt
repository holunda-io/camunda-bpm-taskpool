package io.holunda.polyflow.taskpool.sender.process.variable

import io.holunda.camunda.taskpool.api.process.variable.ProcessVariableValue
import io.holunda.camunda.taskpool.api.task.SourceReference

/**
 * Represents a command on a single process variable.
 * These commands form a sender API, but are not sent
 * to the core.
 */
interface SingleProcessVariableCommand {
  val sourceReference: SourceReference
  val variableName: String
  val variableInstanceId: String
  val scopeActivityInstanceId: String
  val revision: Int
}

/**
 * Creates a new process variable.
 */
data class CreateSingleProcessVariableCommand(
  override val variableInstanceId: String,
  override val variableName: String,
  override val sourceReference: SourceReference,
  override val scopeActivityInstanceId: String,
  override val revision: Int,
  val value: ProcessVariableValue,
) : SingleProcessVariableCommand

/**
 * Updates existing process variable.
 */
data class UpdateSingleProcessVariableCommand(
  override val variableInstanceId: String,
  override val variableName: String,
  override val sourceReference: SourceReference,
  override val scopeActivityInstanceId: String,
  override val revision: Int,
  val value: ProcessVariableValue
) : SingleProcessVariableCommand

/**
 * Deletes a new process variable.
 */
data class DeleteSingleProcessVariableCommand(
  override val variableInstanceId: String,
  override val variableName: String,
  override val sourceReference: SourceReference,
  override val scopeActivityInstanceId: String,
  override val revision: Int
) : SingleProcessVariableCommand
