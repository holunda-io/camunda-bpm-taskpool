package io.holunda.camunda.datapool.sender

import io.holunda.camunda.datapool.DataEntrySenderProperties
import io.holunda.camunda.datapool.projector.DataEntryProjectionSupplier
import io.holunda.camunda.datapool.projector.DataEntryProjector
import io.holunda.camunda.taskpool.api.business.*
import io.holunda.camunda.taskpool.api.business.AuthorizationChange.Companion.addUser
import io.holunda.camunda.variable.serializer.serialize
import org.axonframework.commandhandling.gateway.CommandGateway
import org.slf4j.Logger
import org.slf4j.LoggerFactory

// FIXME: reason about the API and refactor it...
class SimpleDataEntryCommandSender(
  private val gateway: CommandGateway,
  private val properties: DataEntrySenderProperties,
  private val dataEntryProjector: DataEntryProjector
) : DataEntryCommandSender {

  private val logger: Logger = LoggerFactory.getLogger(DataEntryCommandSender::class.java)

  override fun sendDataEntryCommand(
    entryType: EntryType,
    entryId: EntryId,
    payload: Any,
    state: DataEntryState,
    modification: Modification,
    correlations: CorrelationMap) {

    val dataEntryProjectionSupplier: DataEntryProjectionSupplier? = dataEntryProjector.getProjection(entryType)
    val command = CreateOrUpdateDataEntryCommand(
      dataEntryProjectionSupplier?.get()?.apply(entryId, payload) ?: DataEntry(
        entryType = entryType,
        entryId = entryId,
        payload = serialize(payload),
        correlations = correlations,
        name = entryId,
        type = entryType,
        applicationName = properties.applicationName,
        state = state,
        modification = modification,
        authorizations = if (modification.username != null) listOf(addUser(modification.username!!)) else listOf())
    )
    this.sendDataEntryCommand(command = command)
  }

  override fun sendDataEntryCommand(
    entryType: EntryType,
    entryId: EntryId,
    payload: Any,
    name: String,
    description: String,
    type: String,
    state: DataEntryState,
    modification: Modification,
    correlations: CorrelationMap,
    authorizations: List<AuthorizationChange>) {
    val command = CreateOrUpdateDataEntryCommand(
      DataEntry(
        entryType = entryType,
        entryId = entryId,
        payload = serialize(payload),
        correlations = correlations,
        name = name,
        type = type,
        description = description,
        authorizations = authorizations,
        applicationName = properties.applicationName,
        state = state,
        modification = modification
      ))
    this.sendDataEntryCommand(command = command)

  }

  override fun sendDataEntryCommand(command: CreateOrUpdateDataEntryCommand) {
    if (properties.enabled) {
      gateway.send<Any, Any?>(command) { m, r -> logger.debug("Successfully submitted command $m, $r") }
    } else {
      logger.debug("Would have sent command $command")
    }
  }
}

