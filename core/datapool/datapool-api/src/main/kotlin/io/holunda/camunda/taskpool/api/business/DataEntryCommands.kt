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

/**
 * Command to delete the aggregate.
 */
data class DeleteDataEntryCommand(
  /**
   * Entry id.
   */
  val entryId: EntryId,
  /**
   * Entry type.
   */
  val entryType: EntryType,
  /**
   * Modification information to mark the deletion state.
   */
  val modification: Modification = Modification.NONE,
  /**
   * Final state.
   */
  val state: DataEntryState = ProcessingType.DELETED.of(""),
  /**
   * Addressing information.
   */
  @TargetAggregateIdentifier
  val dataIdentity: String = dataIdentityString(entryType = entryType, entryId = entryId),
)

/**
 * Command to anonymize the aggregate.
 */
data class AnonymizeDataEntryCommand(
  /**
   * Entry id.
   */
  val entryId: EntryId,

  /**
   * Entry type.
   */
  val entryType: EntryType,

  /**
   * The username that will replace the current username(s) in the protocol of the data entry
   */
  val anonymizedUsername: String,

  /**
   * Usernames that should be excluded from the anonymization. For example "SYSTEM"
   */
  val excludedUsernames: List<String> = listOf(),

  /**
   * Modification information.
   */
  val anonymizeModification: Modification = Modification.NONE,

  /**
   * Addressing information.
   */
  @TargetAggregateIdentifier
  val dataIdentity: String = dataIdentityString(entryType = entryType, entryId = entryId),
)
