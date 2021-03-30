package io.holunda.camunda.taskpool.api.process.variable

import io.holunda.camunda.taskpool.api.task.SourceReference
import org.axonframework.modelling.command.TargetAggregateIdentifier

/**
 * Represents a command on process variable.
 */
interface ProcessVariableCommand {
  val sourceReference: SourceReference
  val variableName: String
  val variableInstanceId: String
  val scopeActivityInstanceId: String
}

data class CreateProcessVariableCommand(
  @TargetAggregateIdentifier
  override val variableInstanceId: String,
  override val variableName: String,
  override val sourceReference: SourceReference,
  override val scopeActivityInstanceId: String,
  val value: Any,
  val revision: Int
): ProcessVariableCommand

data class UpdateProcessVariableCommand(
  @TargetAggregateIdentifier
  override val variableInstanceId: String,
  override val variableName: String,
  override val sourceReference: SourceReference,
  override val scopeActivityInstanceId: String,
  val value: Any,
  val revision: Int
): ProcessVariableCommand
