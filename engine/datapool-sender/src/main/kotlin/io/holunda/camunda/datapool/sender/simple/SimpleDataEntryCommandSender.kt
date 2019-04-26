package io.holunda.camunda.datapool.sender.simple

import io.holunda.camunda.datapool.DataEntrySenderProperties
import io.holunda.camunda.taskpool.api.business.CorrelationMap
import io.holunda.camunda.taskpool.api.business.CreateOrUpdateDataEntryCommand
import io.holunda.camunda.taskpool.api.business.EntryId
import io.holunda.camunda.taskpool.api.business.EntryType
import io.holunda.camunda.taskpool.api.sender.DataEntryCommandSender
import io.holunda.camunda.variable.serializer.serialize
import org.axonframework.commandhandling.gateway.CommandGateway
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class SimpleDataEntryCommandSender(
  private val gateway: CommandGateway,
  private val properties: DataEntrySenderProperties
) : DataEntryCommandSender {

  val logger: Logger = LoggerFactory.getLogger(DataEntryCommandSender::class.java)

  override fun sendDataEntryCommand(entryType: EntryType, entryId: EntryId, payload: Any, correlations: CorrelationMap) {

    val command = CreateOrUpdateDataEntryCommand(entryType = entryType, entryId = entryId, payload = serialize(payload), correlations = correlations)
    if (properties.enabled) {
      gateway.send<Any, Any?>(command) { m, r -> logger.debug("Successfully submitted command $m, $r") }
    } else {
      logger.debug("Would have sent commandOrUpdate $command")
    }

  }
}

