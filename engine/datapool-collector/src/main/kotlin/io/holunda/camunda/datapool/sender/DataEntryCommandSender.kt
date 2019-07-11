package io.holunda.camunda.datapool.sender

import io.holunda.camunda.taskpool.api.business.*

/**
 * Sends data entry commands.
 */
interface DataEntryCommandSender {

  /**
   * Sends command about data entry creation or update.
   */
  fun sendDataEntryCommand(
    entryType: EntryType,
    entryId: EntryId,
    payload: Any,
    state: DataEntryState = ProcessingType.UNDEFINED.of(),
    modification: Modification = Modification.now(),
    correlations: CorrelationMap = newCorrelations()
  )

  /**
   * Sends command about data entry creation or update.
   */
  fun sendDataEntryCommand(
    entryType: EntryType,
    entryId: EntryId,
    payload: Any,
    name: String,
    description: String,
    type: String,
    state: DataEntryState = ProcessingType.UNDEFINED.of(),
    modification: Modification = Modification.now(),
    correlations: CorrelationMap = newCorrelations(),
    authorizations: List<AuthorizationChange> = listOf()
  )


  /**
   * Sends a data entry command.
   */
  fun sendDataEntryCommand(command: CreateOrUpdateDataEntryCommand)
}
