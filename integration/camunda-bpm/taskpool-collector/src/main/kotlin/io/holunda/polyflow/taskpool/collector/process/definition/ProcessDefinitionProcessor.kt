package io.holunda.polyflow.taskpool.collector.process.definition

import io.holunda.camunda.taskpool.api.process.definition.ProcessDefinitionCommand
import io.holunda.polyflow.taskpool.collector.CamundaTaskpoolCollectorProperties
import io.holunda.polyflow.taskpool.sender.process.definition.ProcessDefinitionCommandSender
import mu.KLogging
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

/**
 * Processes commands sent via Spring Eventing and delegates them to taskpool command sender.
 */
@Component
class ProcessDefinitionProcessor(
  private val processDefinitionCommandSender: ProcessDefinitionCommandSender,
  private val properties: CamundaTaskpoolCollectorProperties
) {
  companion object : KLogging()

  /**
   * Receives the process definition command and pass it over to the sender.
   * @param command process definition command, delivered by Spring eventing.
   */
  @EventListener
  fun process(command: ProcessDefinitionCommand) {
    if (properties.processDefinition.enabled) {
      processDefinitionCommandSender.send(command)
    } else {
      logger.debug { "COLLECTOR-005: Process definition collecting has been disabled by property, skipping ${command.processDefinitionId}." }
    }
  }
}
