package io.holunda.camunda.taskpool.api.business

import org.axonframework.modelling.command.TargetAggregateIdentifier

/**
 * Create or update data entry command.
 */
data class CreateOrUpdateDataEntryCommand(
  val dataEntryChange: DataEntryChange,
  @TargetAggregateIdentifier
  val dataIdentity: String = dataIdentityString(entryType = dataEntryChange.entryType, entryId = dataEntryChange.entryId)
)

/**
 * Internal command to create the aggregate.
 */
data class CreateDataEntryCommand(
  val dataEntryChange: DataEntryChange,
  @TargetAggregateIdentifier
  val dataIdentity: String = dataIdentityString(entryType = dataEntryChange.entryType, entryId = dataEntryChange.entryId)
)

/**
 * Internal command to update the aggregate.
 */
data class UpdateDataEntryCommand(
  val dataEntryChange: DataEntryChange,
  @TargetAggregateIdentifier
  val dataIdentity: String = dataIdentityString(entryType = dataEntryChange.entryType, entryId = dataEntryChange.entryId)
)
