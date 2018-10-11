package io.holunda.camunda.taskpool.api.business

data class DataEntryCreatedEvent(
  override val entryType: EntryType,
  override val entryId: EntryId,
  override val correlations: CorrelationMap = newCorrelations(),
  val payload: Any
) : DataIdentity, WithCorrelations

data class DataEntryUpdatedEvent(
  override val entryType: EntryType,
  override val entryId: EntryId,
  override val correlations: CorrelationMap = newCorrelations(),
  val payload: Any
) : DataIdentity, WithCorrelations
