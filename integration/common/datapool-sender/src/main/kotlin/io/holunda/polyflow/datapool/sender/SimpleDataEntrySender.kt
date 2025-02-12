package io.holunda.polyflow.datapool.sender

import io.holunda.polyflow.datapool.DataEntrySenderProperties
import io.holunda.polyflow.datapool.sender.gateway.CommandListGateway
import mu.KLogging
import org.axonframework.commandhandling.CommandMessage

/**
 * Sends commands using the gateway.
 */
class SimpleDataEntrySender(
  private val commandListGateway: CommandListGateway,
  private val dataEntrySenderProperties: DataEntrySenderProperties
) : DataEntrySender {

  companion object : KLogging()

  override fun <C> send(command: CommandMessage<C>) {
    if(dataEntrySenderProperties.enabled) {
      commandListGateway.sendToGateway(listOf(command))
    } else {
      logger.debug { "SENDER-104: Data entry sending is disabled by property. Would have sent $command." }
    }
  }

}
