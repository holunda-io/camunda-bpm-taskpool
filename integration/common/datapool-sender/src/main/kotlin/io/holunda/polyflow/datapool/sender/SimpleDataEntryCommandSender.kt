package io.holunda.polyflow.datapool.sender

import com.fasterxml.jackson.databind.ObjectMapper
import io.holunda.polyflow.datapool.DataEntrySenderProperties
import io.holunda.polyflow.datapool.projector.DataEntryProjector
import io.holunda.polyflow.datapool.sender.gateway.CommandListGateway
import mu.KLogging
import org.axonframework.commandhandling.CommandMessage

/**
 * Sends commands using the gateway.
 */
class SimpleDataEntryCommandSender(
  private val commandListGateway: CommandListGateway,
  properties: DataEntrySenderProperties,
  dataEntryProjector: DataEntryProjector,
  objectMapper: ObjectMapper
) : AbstractDataEntryCommandSender(properties, dataEntryProjector, objectMapper) {

  override fun <C> send(command: CommandMessage<C>) {
    if (properties.enabled) {
      commandListGateway.sendToGateway(listOf(command))
    } else {
      DataEntryCommandSender.logger.debug { "SENDER-104: Data entry sending is disabled by property. Would have sent $command." }
    }
  }

}
