package io.holunda.polyflow.datapool.sender

import io.holunda.polyflow.datapool.DataEntrySenderProperties
import io.holunda.polyflow.datapool.sender.gateway.CommandListGateway

class DirectTxAwareAccumulatingDataEntryCommandSender(
  private val commandListGateway: CommandListGateway,
  dataEntrySenderProperties: DataEntrySenderProperties
) : TxAwareAccumulatingDataEntryCommandSender(
  dataEntrySenderProperties = dataEntrySenderProperties
) {

  override fun send() {
    // iterate over messages and send them
    dataEntryCommands.get().forEach { (identifier, commands) ->
      logger.debug("SENDER-105: Handling ${commands.size} commands for data entry $identifier")
      // handle messages for every data entry
      if (// FIXME: senderProperties.enabled &&
          dataEntrySenderProperties.enabled) {
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
