package io.holunda.camunda.taskpool.collector.task.enricher

import io.holunda.camunda.taskpool.api.business.EntryType

/**
 * Describes correlation between data entries for a business process definition.
 * @param processDefinitionKey process definition key.
 * @param correlations a map from task definition key to correlation map (variableName, entryType).
 * @param globalCorrelations a global (per-process) correlation map (variableName, entryType).
 */
data class ProcessVariableCorrelation(
  val processDefinitionKey: ProcessDefinitionKey,
  val correlations: Map<TaskDefinitionKey, Map<String, EntryType>>,
  val globalCorrelations: Map<String, EntryType> = emptyMap()
)

