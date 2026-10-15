package io.holunda.camunda.taskpool.api.process.definition

import io.holunda.camunda.taskpool.api.task.WithFormKey
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables
import org.axonframework.modelling.command.TargetAggregateIdentifier

/**
 * Any command about process definition should implement this interface.
 */
interface ProcessDefinitionCommand {
  /**
   * Identifier of the affected process definition.
   */
  val processDefinitionId: String

  /**
   * Stable process-definition key when it is available from the Camunda command context.
   */
  val processDefinitionKey: String?

  /**
   * Additional typed data supplied by a definition command enricher.
   */
  val payload: VariableMap
}

/**
 * Informs about a process definition deployed in the engine.
 */
data class RegisterProcessDefinitionCommand(

  @TargetAggregateIdentifier
  override val processDefinitionId: String,
  override val processDefinitionKey: String,
  val processDefinitionVersion: Int,
  val applicationName: String,
  val processName: String,
  val processVersionTag: String?,
  val processDescription: String?,
  override val formKey: String?,
  val startableFromTasklist: Boolean,
  val candidateStarterUsers: Set<String>,
  val candidateStarterGroups: Set<String>,
  override val payload: VariableMap = Variables.createVariables(),
) : ProcessDefinitionCommand, WithFormKey

/**
 * Signals that a process definition has been suspended.
 */
data class SuspendProcessDefinitionCommand(
  @TargetAggregateIdentifier
  override val processDefinitionId: String,
  override val processDefinitionKey: String? = null,
  override val payload: VariableMap = Variables.createVariables(),
) : ProcessDefinitionCommand

/**
 * Signals that a process definition has been activated.
 */
data class ActivateProcessDefinitionCommand(
  @TargetAggregateIdentifier
  override val processDefinitionId: String,
  override val processDefinitionKey: String? = null,
  override val payload: VariableMap = Variables.createVariables(),
) : ProcessDefinitionCommand

/**
 * Signals that a process definition must be unregistered after Camunda deleted it.
 */
data class UnregisterProcessDefinitionCommand(
  @TargetAggregateIdentifier
  override val processDefinitionId: String,
  override val processDefinitionKey: String? = null,
  override val payload: VariableMap = Variables.createVariables(),
) : ProcessDefinitionCommand
