package io.holunda.camunda.taskpool.collector.process.definition

import io.holunda.camunda.taskpool.api.process.definition.ProcessDefinitionCommand
import io.holunda.camunda.taskpool.collector.CamundaTaskpoolCollectorProperties
import io.holunda.camunda.taskpool.sender.process.definition.ProcessDefinitionCommandSender
import mu.KLogging
import org.axonframework.eventhandling.EventHandler
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

  @EventHandler
  fun process(command: ProcessDefinitionCommand) {
    if (properties.processDefinition.enabled) {
      processDefinitionCommandSender.send(command)
    } else {
      logger.debug { "COLLECTOR-005: Process definition collecting has been disabled by property, skipping ${command.processDefinitionId}." }
    }
  }
}
