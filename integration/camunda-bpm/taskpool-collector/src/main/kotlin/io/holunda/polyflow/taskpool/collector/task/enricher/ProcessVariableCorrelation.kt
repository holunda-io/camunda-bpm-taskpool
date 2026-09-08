package io.holunda.polyflow.taskpool.collector.task.enricher

import io.holunda.camunda.taskpool.api.business.EntryType

/**
 * Describes correlation between data entries for a business process definition.
 * @param processDefinitionKey process definition key.
 * @param correlations a map from task definition key to correlation map (variableName, entryType).
 * @param globalCorrelations a global (per-process) correlation map (variableName, entryType).
 */
data class ProcessVariableCorrelation(
  val processDefinitionKey: ProcessDefinitionKey,
  val correlations: Map<TaskDefinitionKey, List<CorrelationDefinition>> = emptyMap(),
  val globalCorrelations: List<CorrelationDefinition> = emptyList()


) {
  /**
   * Creates a correlation definition from the legacy variable-name-to-entry-type map representation.
   *
   * @param processDefinitionKey process definition key to which the correlations apply.
   * @param correlations task-specific correlations, keyed by task definition key and process variable name.
   * @param globalCorrelations correlations applied to every task in the process.
   */
  @Deprecated("Please use other constructor setting CorrelationDefinition.")
  constructor(
    processDefinitionKey: ProcessDefinitionKey,
    correlations: Map<TaskDefinitionKey, Map<String, EntryType>>,
    globalCorrelations: Map<String, EntryType> = emptyMap()
  ) : this(
    processDefinitionKey = processDefinitionKey,
    correlations = correlations.mapValues { entry -> entry.value.map { CorrelationDefinition(it.value, it.key) } },
    globalCorrelations = globalCorrelations.map { CorrelationDefinition(it.value, it.key) }
  )

}
