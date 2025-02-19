package io.holunda.polyflow.datapool.sender

import com.fasterxml.jackson.databind.ObjectMapper
import io.holunda.polyflow.datapool.DataEntrySenderProperties
import io.holunda.polyflow.datapool.projector.DataEntryProjector
import io.holunda.polyflow.datapool.sender.gateway.CommandListGateway

/**
 * Accumulates commands and sends them directly in the same transaction.
 */
class DirectTxAwareAccumulatingDataEntryCommandSender(
  private val commandListGateway: CommandListGateway,
  properties: DataEntrySenderProperties,
  dataEntryProjector: DataEntryProjector,
  objectMapper: ObjectMapper
) : TxAwareAccumulatingDataEntryCommandSender(
  properties, dataEntryProjector, objectMapper
) {

  override fun send() {
    // iterate over messages and send them
    dataEntryCommands.get().forEach { (identifier, commands) ->
      logger.debug("SENDER-105: Handling ${commands.size} commands for data entry $identifier")
      // handle messages for every data entry
      if (// FIXME: senderProperties.enabled &&
        properties.enabled) {
        commandListGateway.sendToGateway(commands)
        logger.trace {
          "SENDER-TRACE: sending commands for data entry [${commands.first().identifier}]: " + commands.joinToString(
            ", ",
            "'",
            "'",
            -1,
            "..."
          ) { it.commandName }
        }
      } else {
        logger.debug { "SENDER-104: Data entry sending is disabled by property. Would have sent $commands." }
      }
    }
  }
}
