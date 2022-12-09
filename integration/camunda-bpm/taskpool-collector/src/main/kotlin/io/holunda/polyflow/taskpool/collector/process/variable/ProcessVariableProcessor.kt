package io.holunda.polyflow.taskpool.collector.process.variable

import io.holunda.polyflow.taskpool.collector.task.enricher.ProcessVariablesFilter
import io.holunda.polyflow.taskpool.sender.process.variable.ProcessVariableCommandSender
import io.holunda.polyflow.taskpool.sender.process.variable.SingleProcessVariableCommand
import mu.KLogging
import org.springframework.context.event.EventListener

/**
 * Process variable processor responsible for receiving the commands sent via spring eventing and send them to taskpool command sender.
 */
class ProcessVariableProcessor(
  private val processVariableCommandSender: ProcessVariableCommandSender,
  private val processVariablesFilter: ProcessVariablesFilter
) {
  companion object : KLogging()

  /**
   * Reacts on incoming process variable commands.
   * @param command command about process variable to send.
   */
  @EventListener
  fun handle(command: SingleProcessVariableCommand) {

    val isIncluded = processVariablesFilter.isIncluded(
      processDefinitionKey = command.sourceReference.definitionKey,
      command.variableName
    )

    // TODO: implement a variable value transformer. See #310
    if (isIncluded) {
      if (logger.isTraceEnabled) {
        logger.trace { "COLLECTOR-007: Sending process variable command: $command" }
      }
      processVariableCommandSender.send(command)
    }
  }
}
