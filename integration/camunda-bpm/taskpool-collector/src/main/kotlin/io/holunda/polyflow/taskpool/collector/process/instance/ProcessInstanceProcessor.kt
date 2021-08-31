package io.holunda.polyflow.taskpool.collector.process.instance

import io.holunda.camunda.taskpool.api.process.instance.ProcessInstanceCommand
import io.holunda.polyflow.taskpool.collector.CamundaTaskpoolCollectorProperties
import io.holunda.polyflow.taskpool.sender.process.instance.ProcessInstanceCommandSender
import mu.KLogging
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

/**
 * Default process instance processor.
 */
@Component
class ProcessInstanceProcessor(
  private val processInstanceCommandSender: ProcessInstanceCommandSender,
  private val properties: CamundaTaskpoolCollectorProperties
) {
  companion object : KLogging()

  /**
   * Reacts on incoming process instance commands.
   * @param command command about process instance to send.
   */
  @EventListener
  fun process(command: ProcessInstanceCommand) {
    if (properties.processInstance.enabled) {
      processInstanceCommandSender.send(command)
    } else {
      logger.debug { "COLLECTOR-006: Process instance collecting has been disabled by property, skipping ${command.processInstanceId}." }
    }
  }
}
