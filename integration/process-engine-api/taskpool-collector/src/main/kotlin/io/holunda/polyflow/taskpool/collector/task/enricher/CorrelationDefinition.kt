package io.holunda.polyflow.taskpool.collector.task.enricher

import io.holunda.camunda.taskpool.api.business.EntryType

/**
 * Definition of a correlation between a task and a business entry.
 * @param entryIdVariableName name of the process variable containing the entry id.
 * @param entryType type of the business entry.
 */
data class CorrelationDefinition(
  val entryIdVariableName: String,
  val entryType: EntryType
)
