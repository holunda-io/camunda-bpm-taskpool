package io.holunda.camunda.taskpool.api.business

import org.camunda.bpm.engine.variable.VariableMap

/**
 * Data entry created.
 */
data class DataEntryCreatedEvent(
  override val entryType: EntryType,
  override val entryId: EntryId,
  override val correlations: CorrelationMap = newCorrelations(),
  val payload: VariableMap
) : DataIdentity, WithCorrelations

/**
 * Data entry updated.
 */
data class DataEntryUpdatedEvent(
  override val entryType: EntryType,
  override val entryId: EntryId,
  override val correlations: CorrelationMap = newCorrelations(),
  val payload: VariableMap
) : DataIdentity, WithCorrelations
