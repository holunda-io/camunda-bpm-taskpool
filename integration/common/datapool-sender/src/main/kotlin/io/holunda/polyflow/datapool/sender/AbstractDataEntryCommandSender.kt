package io.holunda.polyflow.datapool.sender

import com.fasterxml.jackson.databind.ObjectMapper
import io.holunda.camunda.taskpool.api.business.*
import io.holunda.camunda.variable.serializer.serialize
import io.holunda.polyflow.datapool.DataEntrySenderProperties
import io.holunda.polyflow.datapool.projector.DataEntryProjectionSupplier
import io.holunda.polyflow.datapool.projector.DataEntryProjector
import org.axonframework.commandhandling.CommandMessage
import org.axonframework.commandhandling.GenericCommandMessage
import org.axonframework.messaging.MetaData
import org.camunda.bpm.engine.variable.VariableMap
import org.slf4j.Logger
import org.slf4j.LoggerFactory

// FIXME: reason about the API and refactor it...
/**
 * Simple data entry command sender.
 */
abstract class AbstractDataEntryCommandSender(
  val properties: DataEntrySenderProperties,
  private val dataEntryProjector: DataEntryProjector,
  private val objectMapper: ObjectMapper
) : DataEntryCommandSender {

  private val logger: Logger = LoggerFactory.getLogger(DataEntryCommandSender::class.java)

  /** to be more java friendly **/
  override fun sendDataEntryChange(
    entryType: EntryType,
    entryId: EntryId,
    payload: Any,
    name: String,
    description: String?,
    type: String,
    state: DataEntryState
  ) = sendDataEntryChange(
    entryType = entryType,
    entryId = entryId,
    payload = payload,
    name = name,
    description = description,
    type = type,
    state = state,
    modification = Modification.now(),
    correlations = newCorrelations(),
    authorizationChanges = listOf(),
    metaData = MetaData.emptyInstance()
  )

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
    metaData: MetaData
  ) {

    val dataEntryProjectionSupplier: DataEntryProjectionSupplier? = dataEntryProjector.getProjection(entryType)

    val command = CreateOrUpdateDataEntryCommand(
      dataEntryProjectionSupplier?.get()?.apply(entryId, payload) ?: DataEntryChange(
        entryType = entryType,
        entryId = entryId,
        payload = if (properties.serializePayload) {
          serialize(payload = payload, mapper = objectMapper)
        } else {
          if (payload is VariableMap) {
            payload
          } else {
            throw IllegalArgumentException("Property for payload serialization is set to false, expected payload must be VariableMap but it was ${payload.javaClass.canonicalName}")
          }
        },
        correlations = correlations,
        name = name,
        type = type,
        description = description,
        authorizationChanges = authorizationChanges,
        applicationName = properties.applicationName,
        state = state,
        modification = modification
      )
    )
    this.sendDataEntryChange(command = command, metaData = metaData)
  }

  override fun sendDataEntryChange(command: CreateOrUpdateDataEntryCommand, metaData: MetaData) {
    if (properties.enabled) {
      val message = GenericCommandMessage
        .asCommandMessage<CreateOrUpdateDataEntryCommand>(command)
        .withMetaData(metaData)
      send(message)
    } else {
      logger.debug("Would have sent change command $command")
    }
  }

  override fun sendDataEntryDelete(command: DeleteDataEntryCommand, metaData: MetaData) {
    if (properties.enabled) {
      val message = GenericCommandMessage
        .asCommandMessage<DeleteDataEntryCommand>(command)
        .withMetaData(metaData)
      send(message)
    } else {
      logger.debug("Would have sent delete command $command")
    }
  }

  override fun sendDataEntryAnonymize(command: AnonymizeDataEntryCommand, metaData: MetaData) {
    if (properties.enabled) {
      val message = GenericCommandMessage
        .asCommandMessage<AnonymizeDataEntryCommand>(command)
        .withMetaData(metaData)
      send(message)
    } else {
      logger.debug("Would have sent anonymize command $command")
    }
  }

  abstract fun <C> send(command: CommandMessage<C>)
}

