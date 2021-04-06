package io.holunda.camunda.taskpool.collector.process.variable

import io.holunda.camunda.taskpool.api.process.variable.ProcessVariableCommand
import io.holunda.camunda.taskpool.collector.CamundaTaskpoolCollectorProperties
import io.holunda.camunda.taskpool.sender.process.variable.ProcessVariableCommandSender
import mu.KLogging
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

/**
 * Process variable processor responsible for receiving the commands sent via spring eventing and send them to taskpool command sender.
 */
@Component
class ProcessVariableProcessor(
  private val processVariableCommandSender: ProcessVariableCommandSender,
  private val properties: CamundaTaskpoolCollectorProperties
) {
  companion object : KLogging()

  /**
   * Reacts on incoming process variable commands.
   * @param command command about process variable to send.
   */
  @EventListener
  fun handle(command: ProcessVariableCommand) {
    if (properties.processVariable.enabled) {
      processVariableCommandSender.send(command)
    } else {
      logger.debug { "COLLECTOR-007: Process variable collecting has been disabled by property, skipping ${command.variableName}." }
    }
  }
}
