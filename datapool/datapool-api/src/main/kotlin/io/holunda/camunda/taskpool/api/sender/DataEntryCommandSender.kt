package io.holunda.camunda.taskpool.api.sender

import io.holunda.camunda.taskpool.api.business.*

/**
 * Sends data entry commands.
 */
interface DataEntryCommandSender {

  /**
   * Sends command about data entry creation or update.
   */
  fun sendDataEntryCommand(entryType: EntryType, entryId: EntryId, payload: Any, correlations: CorrelationMap = newCorrelations())
}
