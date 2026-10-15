package io.holunda.polyflow.view

import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables

/**
 * Lifecycle state of a process definition.
 */
enum class ProcessDefinitionState {
  /**
   * The definition can start process instances.
   */
  ACTIVE,

  /**
   * The definition cannot start process instances.
   */
  SUSPENDED,
}

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
  val candidateStarterGroups: Set<String> = setOf(),
  val state: ProcessDefinitionState = ProcessDefinitionState.ACTIVE,
  val payload: VariableMap = Variables.createVariables(),
) {
  /**
   * Retains the JVM constructor exposed before lifecycle state and payload were added.
   */
  constructor(
    legacyProcessDefinitionId: String,
    legacyProcessDefinitionKey: String,
    legacyProcessDefinitionVersion: Int,
    legacyApplicationName: String,
    legacyProcessName: String,
    legacyProcessVersionTag: String? = null,
    legacyProcessDescription: String? = null,
    legacyFormKey: String? = null,
    legacyStartableFromTasklist: Boolean = true,
    legacyCandidateStarterUsers: Set<String> = setOf(),
    legacyCandidateStarterGroups: Set<String> = setOf(),
  ) : this(
    processDefinitionId = legacyProcessDefinitionId,
    processDefinitionKey = legacyProcessDefinitionKey,
    processDefinitionVersion = legacyProcessDefinitionVersion,
    applicationName = legacyApplicationName,
    processName = legacyProcessName,
    processVersionTag = legacyProcessVersionTag,
    processDescription = legacyProcessDescription,
    formKey = legacyFormKey,
    startableFromTasklist = legacyStartableFromTasklist,
    candidateStarterUsers = legacyCandidateStarterUsers,
    candidateStarterGroups = legacyCandidateStarterGroups,
  )

  /**
   * Retains the JVM copy function exposed before lifecycle state and payload were added.
   */
  fun copy(
    legacyProcessDefinitionId: String = processDefinitionId,
    legacyProcessDefinitionKey: String = processDefinitionKey,
    legacyProcessDefinitionVersion: Int = processDefinitionVersion,
    legacyApplicationName: String = applicationName,
    legacyProcessName: String = processName,
    legacyProcessVersionTag: String? = processVersionTag,
    legacyProcessDescription: String? = processDescription,
    legacyFormKey: String? = formKey,
    legacyStartableFromTasklist: Boolean = startableFromTasklist,
    legacyCandidateStarterUsers: Set<String> = candidateStarterUsers,
    legacyCandidateStarterGroups: Set<String> = candidateStarterGroups,
  ): ProcessDefinition = ProcessDefinition(
    processDefinitionId = legacyProcessDefinitionId,
    processDefinitionKey = legacyProcessDefinitionKey,
    processDefinitionVersion = legacyProcessDefinitionVersion,
    applicationName = legacyApplicationName,
    processName = legacyProcessName,
    processVersionTag = legacyProcessVersionTag,
    processDescription = legacyProcessDescription,
    formKey = legacyFormKey,
    startableFromTasklist = legacyStartableFromTasklist,
    candidateStarterUsers = legacyCandidateStarterUsers,
    candidateStarterGroups = legacyCandidateStarterGroups,
    state = state,
    payload = payload,
  )
}
