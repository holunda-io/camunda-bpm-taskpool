package io.holunda.camunda.taskpool.api.business

import org.axonframework.modelling.command.TargetAggregateIdentifier


data class CreateOrUpdateDataEntryCommand(
  val dataEntry: DataEntry,
  @TargetAggregateIdentifier
  val dataIdentity: String = dataIdentityString(entryType = dataEntry.entryType, entryId = dataEntry.entryId)
)

data class CreateDataEntryCommand(
  val dataEntry: DataEntry,
  @TargetAggregateIdentifier
  val dataIdentity: String = dataIdentityString(entryType = dataEntry.entryType, entryId = dataEntry.entryId)
)

data class UpdateDataEntryCommand(
  val dataEntry: DataEntry,
  @TargetAggregateIdentifier
  val dataIdentity: String = dataIdentityString(entryType = dataEntry.entryType, entryId = dataEntry.entryId)
)
