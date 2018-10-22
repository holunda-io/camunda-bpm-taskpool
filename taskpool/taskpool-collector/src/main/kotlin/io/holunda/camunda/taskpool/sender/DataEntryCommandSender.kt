package io.holunda.camunda.taskpool.sender

import io.holunda.camunda.taskpool.TaskCollectorProperties
import io.holunda.camunda.taskpool.api.business.CreateOrUpdateDataEntryCommand
import mu.KLogging
import org.axonframework.commandhandling.CommandCallback
import org.axonframework.commandhandling.CommandMessage
import org.axonframework.commandhandling.gateway.CommandGateway
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class DataEntryCommandSender(
  private val gateway: CommandGateway,
  private val properties: TaskCollectorProperties
  ) {
  companion object : KLogging()

  @EventListener
  fun sendDataEntryCommand(commandOrUpdate: CreateOrUpdateDataEntryCommand) {

    if (properties.sender.enabled) {
      gateway.send<Any, Any?>(commandOrUpdate, object : CommandCallback<Any, Any?> {
        override fun onSuccess(m: CommandMessage<out Any>, result: Any?) {
          logger.debug("Successfully submitted commandOrUpdate $commandOrUpdate")
        }
        override fun onFailure(message: CommandMessage<out Any>?, e: Throwable) {
          logger.error("Error sending commandOrUpdate $message", e)
        }
      })
    } else {
      logger.debug("Would have sent commandOrUpdate $commandOrUpdate")
    }

  }
}
