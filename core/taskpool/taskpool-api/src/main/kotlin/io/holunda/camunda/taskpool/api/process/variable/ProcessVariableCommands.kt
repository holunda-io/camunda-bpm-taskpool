package io.holunda.camunda.taskpool.api.process.variable

import io.holunda.camunda.taskpool.api.task.SourceReference
import org.axonframework.modelling.command.TargetAggregateIdentifier
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables.createVariables

/**
 * Represents a command on process variable.
 */
interface ProcessVariableCommand {
  val sourceReference: SourceReference
  val variableName: String
  val variableInstanceId: String
  val scopeActivityInstanceId: String
}

/**
 * Creates a new process variable.
 */
data class CreateProcessVariableCommand(
  @TargetAggregateIdentifier
  override val variableInstanceId: String,
  override val variableName: String,
  override val sourceReference: SourceReference,
  override val scopeActivityInstanceId: String,
  val value: ProcessVariableValue,
  val revision: Int
): ProcessVariableCommand

/**
 * Updates existing process variable.
 */
data class UpdateProcessVariableCommand(
  @TargetAggregateIdentifier
  override val variableInstanceId: String,
  override val variableName: String,
  override val sourceReference: SourceReference,
  override val scopeActivityInstanceId: String,
  val value: ProcessVariableValue,
  val revision: Int
): ProcessVariableCommand

/**
 * Deletes a new process variable.
 */
data class DeleteProcessVariableCommand(
  @TargetAggregateIdentifier
  override val variableInstanceId: String,
  override val variableName: String,
  override val sourceReference: SourceReference,
  override val scopeActivityInstanceId: String
): ProcessVariableCommand
