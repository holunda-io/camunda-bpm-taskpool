package io.holunda.camunda.taskpool.collector.process.definition

import io.holunda.camunda.taskpool.CamundaTaskpoolCollectorProperties
import io.holunda.camunda.taskpool.api.process.definition.ProcessDefinitionCommand
import io.holunda.camunda.taskpool.sender.process.definition.ProcessDefinitionCommandSender
import mu.KLogging
import org.springframework.stereotype.Component

@Component
class ProcessDefinitionProcessorService(
  private val processDefinitionCommandSender: ProcessDefinitionCommandSender,
  private val properties: CamundaTaskpoolCollectorProperties
) {
  companion object: KLogging()

  fun send(command: ProcessDefinitionCommand) {
    if (properties.processDefinition.enabled) {
      logger.debug { "Sending update about process definition ${command.processDefinitionId}." }
      processDefinitionCommandSender.send(command)
    } else {
      logger.debug { "Process definition collecting has been disabled by property, skipping ${command.processDefinitionId}." }
    }
  }
}
