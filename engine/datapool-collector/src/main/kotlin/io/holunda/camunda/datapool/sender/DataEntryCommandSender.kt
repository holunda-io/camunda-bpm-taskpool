package io.holunda.camunda.datapool.sender

import io.holunda.camunda.taskpool.api.business.*
import org.axonframework.commandhandling.CommandResultMessage
import org.axonframework.messaging.MetaData
import java.util.function.BiFunction

/**
 * Sends data entry commands.
 */
interface DataEntryCommandSender {

  /**
   * Sends command about data entry creation or update.
   * @param metaData meta data, will default to empty metadata.
   */
  fun sendDataEntryCommand(
    entryType: EntryType,
    entryId: EntryId,
    payload: Any,
    state: DataEntryState = ProcessingType.UNDEFINED.of(),
    modification: Modification = Modification.now(),
    correlations: CorrelationMap = newCorrelations(),
    metaData: MetaData = MetaData.emptyInstance()
  )

  /**
   * Sends command about data entry creation or update.
   * @param metaData meta data, will default to empty metadata.
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
    authorizations: List<AuthorizationChange> = listOf(),
    metaData: MetaData = MetaData.emptyInstance()
  )


  /**
   * Sends a data entry command.
   * @param command command to send.
   * @param metaData meta data, will default to empty metadata.
   */
  fun sendDataEntryCommand(command: CreateOrUpdateDataEntryCommand, metaData: MetaData = MetaData.emptyInstance())
}

/**
 * Success handler for commands.
 */
interface DataEntryCommandSuccessHandler: BiFunction<Any, CommandResultMessage<out Any?>, Unit>
/**
 * Error handler for commands.
 */
interface DataEntryCommandErrorHandler: BiFunction<Any, CommandResultMessage<out Any?>, Unit>
