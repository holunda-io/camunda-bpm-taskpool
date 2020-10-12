package io.holunda.camunda.taskpool.enricher

import io.holunda.camunda.taskpool.TaskCollectorProperties
import io.holunda.camunda.taskpool.api.process.instance.ProcessInstanceCommand
import io.holunda.camunda.taskpool.sender.gateway.CommandListGateway
import mu.KLogging
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

/**
 * Default process instance enricher.
 */
@Component
class ProcessInstanceEnricherService(
  private val commandListGateway: CommandListGateway,
  private val properties: TaskCollectorProperties
) {
  companion object : KLogging()

  /**
   * Reacts on incoming process instance commands.
   * @param command command about process instance to send.
   */
  @EventListener
  fun handle(command: ProcessInstanceCommand) {
    if (properties.processInstance.enabled) {
      commandListGateway.sendToGateway(listOf(command))
      logger.debug { "Sending update about process instance ${command.processInstanceId}." }
    } else {
      logger.debug { "Process instance collecting has been disabled by property, skipping ${command.processInstanceId}." }
    }
  }
}
