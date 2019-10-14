package io.holunda.camunda.datapool.sender

import io.holunda.camunda.taskpool.api.business.*
import org.axonframework.commandhandling.CommandResultMessage
import java.util.function.BiFunction

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

/**
 * Success handler for commands.
 */
interface DataEntryCommandSuccessHandler: BiFunction<Any, CommandResultMessage<out Any?>, Unit>
/**
 * Error handler for commands.
 */
interface DataEntryCommandErrorHandler: BiFunction<Any, CommandResultMessage<out Any?>, Unit>
