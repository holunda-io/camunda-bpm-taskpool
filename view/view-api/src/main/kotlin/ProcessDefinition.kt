package io.holunda.polyflow.view

/**
 * Represents a deployed process definition.
 */
data class ProcessDefinition(
  val processDefinitionId: String,
  val processDefinitionKey: String,
  val processDefinitionVersion: Int,

  val applicationName: String,
  val processName: String,
  val processVersionTag: String? = null,
  val processDescription: String? = null,
  val formKey: String? = null,
  val startableFromTasklist: Boolean = true,
  val candidateStarterUsers: Set<String> = setOf(),
  val candidateStarterGroups: Set<String> = setOf()
)
