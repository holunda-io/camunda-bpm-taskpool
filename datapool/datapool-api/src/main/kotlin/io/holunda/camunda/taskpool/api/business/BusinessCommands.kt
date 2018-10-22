package io.holunda.camunda.taskpool.api.business

import org.axonframework.modelling.command.TargetAggregateIdentifier
import org.camunda.bpm.engine.variable.VariableMap

data class CreateOrUpdateDataEntryCommand(
  override val entryType: EntryType,
  override val entryId: EntryId,
  override val correlations: CorrelationMap = newCorrelations(),
  @TargetAggregateIdentifier
  val dataIdentity: String = dataIdentity(entryType = entryType, entryId = entryId),
  val payload: VariableMap
) : DataIdentity, WithCorrelations

data class CreateDataEntryCommand(
  override val entryType: EntryType,
  override val entryId: EntryId,
  override val correlations: CorrelationMap = newCorrelations(),
  @TargetAggregateIdentifier
  val dataIdentity: String = dataIdentity(entryType = entryType, entryId = entryId),

  val payload: VariableMap
) : DataIdentity, WithCorrelations

data class UpdateDataEntryCommand(
  override val entryType: EntryType,
  override val entryId: EntryId,
  override val correlations: CorrelationMap = newCorrelations(),
  @TargetAggregateIdentifier
  val dataIdentity: String = dataIdentity(entryType = entryType, entryId = entryId),

  val payload: VariableMap
) : DataIdentity, WithCorrelations
