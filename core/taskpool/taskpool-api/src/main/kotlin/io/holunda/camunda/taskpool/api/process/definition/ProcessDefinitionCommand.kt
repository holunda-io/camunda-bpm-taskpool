package io.holunda.camunda.taskpool.api.process.definition

import io.holunda.camunda.taskpool.api.task.WithFormKey
import org.axonframework.modelling.command.TargetAggregateIdentifier

/**
 * Any command about process definition should implement this interface.
 */
interface ProcessDefinitionCommand {
  val processDefinitionId: String
}

/**
 * Informs about a process definition deployed in the engine.
 */
data class RegisterProcessDefinitionCommand(

  @TargetAggregateIdentifier
  override val processDefinitionId: String,
  val processDefinitionKey: String,
  val processDefinitionVersion: Int,

  val applicationName: String,
  val processName: String,
  val processVersionTag: String?,
  val processDescription: String?,
  override val formKey: String?,
  val startableFromTasklist: Boolean,
  val candidateStarterUsers: Set<String>,
  val candidateStarterGroups: Set<String>
) : ProcessDefinitionCommand, WithFormKey
