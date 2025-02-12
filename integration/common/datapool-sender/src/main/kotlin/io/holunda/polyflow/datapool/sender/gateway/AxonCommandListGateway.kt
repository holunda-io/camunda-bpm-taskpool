package io.holunda.polyflow.datapool.sender.gateway

import io.holunda.polyflow.datapool.DataEntrySenderProperties
import mu.KLogging
import org.axonframework.commandhandling.gateway.CommandGateway

// FIXME: extract together with io.holunda.polyflow.taskpool.sender.gateway.AxonCommandListGateway
/**
 * Sends  a list commands via AXON command gateway one-by-one, only if the sender property is enabled.
 */
class AxonCommandListGateway(
  private val commandGateway: CommandGateway,
  private val senderProperties: DataEntrySenderProperties, // FIXME: should be SenderPorperties
  private val commandSuccessHandler: CommandSuccessHandler,
  private val commandErrorHandler: CommandErrorHandler
) : CommandListGateway {

  companion object : KLogging()

  /**
   * Sends data to gateway. Ignores any errors, but logs.
   */
  override fun sendToGateway(commands: List<Any>) {
    if (commands.isNotEmpty()) {

      val nextCommand = commands.first()
      val remainingCommands = commands.subList(1, commands.size)

      if (senderProperties.enabled) {
        commandGateway.send<Any, Any?>(nextCommand) { commandMessage, commandResultMessage ->
          if (commandResultMessage.isExceptional) {
            commandErrorHandler.apply(commandMessage, commandResultMessage)
          } else {
            commandSuccessHandler.apply(commandMessage, commandResultMessage)
          }
          sendToGateway(remainingCommands)
        }
      } else {
        logger.debug("SENDER-101: Sending command over gateway disabled by property. Would have sent command $nextCommand")
        sendToGateway(remainingCommands)
      }
    }
  }

}



