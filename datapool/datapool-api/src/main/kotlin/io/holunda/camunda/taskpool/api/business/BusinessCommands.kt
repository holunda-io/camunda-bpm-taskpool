package io.holunda.camunda.taskpool.api.business

import org.axonframework.modelling.command.TargetAggregateIdentifier
import org.camunda.bpm.engine.variable.VariableMap
import java.time.OffsetDateTime


data class CreateOrUpdateDataEntryCommand(
  val dataEntry: DataEntry,
  @TargetAggregateIdentifier
  val dataIdentity: String = dataIdentity(entryType = dataEntry.entryType, entryId = dataEntry.entryId)
)

data class CreateDataEntryCommand(
  val dataEntry: DataEntry,
  @TargetAggregateIdentifier
  val dataIdentity: String = dataIdentity(entryType = dataEntry.entryType, entryId = dataEntry.entryId)
)

data class UpdateDataEntryCommand(
  val dataEntry: DataEntry,
  @TargetAggregateIdentifier
  val dataIdentity: String = dataIdentity(entryType = dataEntry.entryType, entryId = dataEntry.entryId)
)
