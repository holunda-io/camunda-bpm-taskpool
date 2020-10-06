package io.holunda.camunda.datapool.sender

import com.fasterxml.jackson.databind.ObjectMapper
import io.holunda.camunda.datapool.DataEntrySenderProperties
import io.holunda.camunda.datapool.projector.DataEntryProjectionSupplier
import io.holunda.camunda.datapool.projector.DataEntryProjector
import io.holunda.camunda.taskpool.api.business.*
import io.holunda.camunda.variable.serializer.serialize
import org.axonframework.commandhandling.CommandResultMessage
import org.axonframework.commandhandling.GenericCommandMessage
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.MetaData
import org.slf4j.Logger
import org.slf4j.LoggerFactory

// FIXME: reason about the API and refactor it...
/**
 * Simple data entry command sender.
 */
class SimpleDataEntryCommandSender(
  private val gateway: CommandGateway,
  private val properties: DataEntrySenderProperties,
  private val dataEntryProjector: DataEntryProjector,
  private val successHandler: DataEntryCommandSuccessHandler,
  private val errorHandler: DataEntryCommandErrorHandler,
  private val objectMapper: ObjectMapper
) : DataEntryCommandSender {

  private val logger: Logger = LoggerFactory.getLogger(DataEntryCommandSender::class.java)

  override fun sendDataEntryChange(
    entryType: EntryType,
    entryId: EntryId,
    payload: Any,
    name: String,
    description: String?,
    type: String,
    state: DataEntryState,
    modification: Modification,
    correlations: CorrelationMap,
    authorizationChanges: List<AuthorizationChange>,
    metaData: MetaData)
  {

    val dataEntryProjectionSupplier: DataEntryProjectionSupplier? = dataEntryProjector.getProjection(entryType)

    val command = CreateOrUpdateDataEntryCommand(
      dataEntryProjectionSupplier?.get()?.apply(entryId, payload) ?:
      DataEntryChange(
        entryType = entryType,
        entryId = entryId,
        payload = serialize(payload = payload, mapper = objectMapper),
        correlations = correlations,
        name = name,
        type = type,
        description = description,
        authorizationChanges = authorizationChanges,
        applicationName = properties.applicationName,
        state = state,
        modification = modification
      ))
    this.sendDataEntryChange(command = command, metaData = metaData)
  }

  override fun sendDataEntryChange(command: CreateOrUpdateDataEntryCommand, metaData: MetaData) {
    if (properties.enabled) {
      val message = GenericCommandMessage
        .asCommandMessage<CreateOrUpdateDataEntryCommand>(command)
        .withMetaData(metaData)
      gateway.send<Any, Any?>(message) { m, r ->
        if (r.isExceptional) {
          errorHandler.apply(m, r)
        } else {
          successHandler.apply(m, r)
        }
      }
    } else {
      logger.debug("Would have sent command $command")
    }
  }
}

/**
 * Success handler, logging.
 */
class LoggingCommandSuccessHandler(private val logger: Logger) : DataEntryCommandSuccessHandler {

  override fun apply(commandMessage: Any, commandResultMessage: CommandResultMessage<out Any?>) {
    if (logger.isDebugEnabled) {
      logger.debug("Successfully submitted command $commandMessage, $commandResultMessage")
    }
  }
}

/**
 * Error handler, logging the error.
 */
class LoggingCommandErrorHandler(private val logger: Logger) : DataEntryCommandErrorHandler {

  override fun apply(commandMessage: Any, commandResultMessage: CommandResultMessage<out Any?>) {
    logger.error("SENDER-006: Sending command $commandMessage resulted in error", commandResultMessage.exceptionResult())
  }
}
