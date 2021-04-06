package io.holunda.camunda.taskpool.sender.process.variable

import com.fasterxml.jackson.databind.ObjectMapper
import io.holunda.camunda.taskpool.api.process.variable.CreateProcessVariableCommand
import io.holunda.camunda.taskpool.api.process.variable.ProcessVariableCommand
import io.holunda.camunda.taskpool.api.process.variable.UpdateProcessVariableCommand
import io.holunda.camunda.taskpool.sender.SenderProperties
import io.holunda.camunda.taskpool.sender.gateway.CommandListGateway
import io.holunda.camunda.taskpool.serialize
import mu.KLogging

/**
 * Simple sender for process variable commands.
 */
internal class SimpleProcessVariableCommandSender(
  private val commandListGateway: CommandListGateway,
  private val senderProperties: SenderProperties,
  private val objectMapper: ObjectMapper
): ProcessVariableCommandSender {
  companion object : KLogging()

  override fun send(command: ProcessVariableCommand) {
    if (senderProperties.processVariable.enabled) {
      commandListGateway.sendToGateway(listOf(
        when (command) {
          is CreateProcessVariableCommand -> command.copy(value = command.value.serialize(objectMapper))
          is UpdateProcessVariableCommand -> command.copy(value = command.value.serialize(objectMapper))
          else -> command
        }
      ))
    } else {
      logger.debug { "SENDER-009: Process variable sending is disabled by property. Would have sent $command." }
    }
  }
}
