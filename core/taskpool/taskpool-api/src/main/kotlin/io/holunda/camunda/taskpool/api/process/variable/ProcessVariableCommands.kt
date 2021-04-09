package io.holunda.camunda.taskpool.api.process.variable

import io.holunda.camunda.taskpool.api.task.SourceReference
import org.axonframework.modelling.command.TargetAggregateIdentifier
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables.createVariables

/**
 * Represents a change of process variables of a given process execution.
 */
data class ChangeProcessVariablesForExecutionCommand(
  val sourceReference: SourceReference,
  val variableChanges: List<ProcessVariableChange>
) {
  @TargetAggregateIdentifier
  val processInstanceId: String = sourceReference.instanceId
}

/**
 * Variable change.
 */
interface ProcessVariableChange {
  val variableName: String
  val variableInstanceId: String
  val revision: Int
  val scopeActivityInstanceId: String
}

/**
 * Change exposing the creating of variable.
 */
data class ProcessVariableCreate(
  override val variableInstanceId: String,
  override val variableName: String,
  override val revision: Int,
  override val scopeActivityInstanceId: String,
  val value: ProcessVariableValue,
) : ProcessVariableChange

/**
 * Change exposing the updating of the variable.
 */
data class ProcessVariableUpdate(
  override val variableInstanceId: String,
  override val variableName: String,
  override val revision: Int,
  override val scopeActivityInstanceId: String,
  val value: ProcessVariableValue,
) : ProcessVariableChange

/**
 * Change exposing the deleting of variable.
 */
data class ProcessVariableDelete(
  override val variableInstanceId: String,
  override val variableName: String,
  override val revision: Int,
  override val scopeActivityInstanceId: String
) : ProcessVariableChange
