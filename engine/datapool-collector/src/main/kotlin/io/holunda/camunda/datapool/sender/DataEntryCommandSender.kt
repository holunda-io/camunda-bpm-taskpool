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
   * @param entryType type of entry.
   * @param entryId id of entry.
   * @param payload payload to send,
   * @param name human readable name of the data entry.
   * @param type human readable type of the entry.
   * @param state new state of the entry.
   * @param modification details about the change, will be added to protocol.
   * @param authorizationChanges changes of the authorizations.
   * @param correlations correlations to other data entries.
   * @param metaData meta data, will default to empty metadata.
   */
  fun sendDataEntryChange(
    entryType: EntryType,
    entryId: EntryId,
    payload: Any = mapOf<String, Any>(),
    name: String = entryId,
    description: String? = null,
    type: String = entryType,
    state: DataEntryState = ProcessingType.UNDEFINED.of(),
    modification: Modification = Modification.now(),
    correlations: CorrelationMap = newCorrelations(),
    authorizationChanges: List<AuthorizationChange> = if (modification.username != null) listOf(AuthorizationChange.addUser(modification.username!!)) else listOf(),
    metaData: MetaData = MetaData.emptyInstance()
  )

  /**
   * Sends a data entry command.
   * @param command command to send.
   * @param metaData meta data, will default to empty metadata.
   */
  fun sendDataEntryChange(command: CreateOrUpdateDataEntryCommand, metaData: MetaData = MetaData.emptyInstance())
}

/**
 * Success handler for commands.
 */
interface DataEntryCommandSuccessHandler : BiFunction<Any, CommandResultMessage<out Any?>, Unit>

/**
 * Error handler for commands.
 */
interface DataEntryCommandErrorHandler : BiFunction<Any, CommandResultMessage<out Any?>, Unit>
