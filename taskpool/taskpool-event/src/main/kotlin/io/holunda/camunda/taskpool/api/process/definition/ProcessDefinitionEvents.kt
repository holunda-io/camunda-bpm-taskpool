package io.holunda.camunda.taskpool.api.process.definition

import org.axonframework.serialization.Revision

/**
 * Event sent after a new process has been deployed.
 */
@Revision("1")
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
  val candidateStarterGroups: Set<String> = setOf()
)
