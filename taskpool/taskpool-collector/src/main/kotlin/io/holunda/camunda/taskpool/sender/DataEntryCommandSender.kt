package io.holunda.camunda.taskpool.sender

import io.holunda.camunda.taskpool.TaskCollectorProperties
import io.holunda.camunda.taskpool.api.business.CreateOrUpdateDataEntryCommand
import mu.KLogging
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
      gateway.send<Any, Any?>(commandOrUpdate) { m, r -> logger.debug("Successfully submitted commandOrUpdate $m, $r") }
    } else {
      logger.debug("Would have sent commandOrUpdate $commandOrUpdate")
    }

  }
}
