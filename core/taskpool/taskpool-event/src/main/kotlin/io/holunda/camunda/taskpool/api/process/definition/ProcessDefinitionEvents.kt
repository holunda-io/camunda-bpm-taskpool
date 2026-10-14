package io.holunda.camunda.taskpool.api.process.definition

import org.axonframework.serialization.Revision
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables

/**
 * Event sent after a new process has been deployed.
 */
@Revision("2")
data class ProcessDefinitionRegisteredEvent(
  val processDefinitionId: String,
  val processDefinitionKey: String,
  val processDefinitionVersion: Int,
  val applicationName: String,
  val processName: String,
  val processVersionTag: String?,
  val processDescription: String?,
  val formKey: String?,
  val startableFromTasklist: Boolean,
  val candidateStarterUsers: Set<String> = setOf(),
  val candidateStarterGroups: Set<String> = setOf(),
  val payload: VariableMap = Variables.createVariables(),
)

/**
 * Event emitted when a process definition becomes suspended.
 */
@Revision("2")
data class ProcessDefinitionSuspendedEvent(
  val processDefinitionId: String,
  val payload: VariableMap = Variables.createVariables(),
)

/**
 * Event emitted when a process definition becomes active.
 */
@Revision("2")
data class ProcessDefinitionActivatedEvent(
  val processDefinitionId: String,
  val payload: VariableMap = Variables.createVariables(),
)

/**
 * Event emitted when a process definition is unregistered after it was removed from Camunda.
 */
@Revision("2")
data class ProcessDefinitionUnregisteredEvent(
  val processDefinitionId: String,
  val payload: VariableMap = Variables.createVariables(),
)
