package io.holunda.camunda.taskpool.api.business

import org.axonframework.modelling.command.TargetAggregateIdentifier

/**
 * Create or update data entry command.
 */
data class CreateOrUpdateDataEntryCommand(
  val dataEntry: DataEntry,
  @TargetAggregateIdentifier
  val dataIdentity: String = dataIdentityString(entryType = dataEntry.entryType, entryId = dataEntry.entryId)
)

/**
 * Internal command to create the aggregate.
 */
data class CreateDataEntryCommand(
  val dataEntry: DataEntry,
  @TargetAggregateIdentifier
  val dataIdentity: String = dataIdentityString(entryType = dataEntry.entryType, entryId = dataEntry.entryId)
)

/**
 * Internal command to update the aggregate.
 */
data class UpdateDataEntryCommand(
  val dataEntry: DataEntry,
  @TargetAggregateIdentifier
  val dataIdentity: String = dataIdentityString(entryType = dataEntry.entryType, entryId = dataEntry.entryId)
)
