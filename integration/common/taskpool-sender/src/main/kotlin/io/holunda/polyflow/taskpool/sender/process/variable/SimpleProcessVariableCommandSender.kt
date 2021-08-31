package io.holunda.polyflow.taskpool.sender.process.variable

import com.fasterxml.jackson.databind.ObjectMapper
import io.holunda.camunda.taskpool.api.process.variable.ChangeProcessVariablesForExecutionCommand
import io.holunda.camunda.taskpool.api.process.variable.ProcessVariableCreate
import io.holunda.camunda.taskpool.api.process.variable.ProcessVariableDelete
import io.holunda.camunda.taskpool.api.process.variable.ProcessVariableUpdate
import io.holunda.polyflow.taskpool.sender.SenderProperties
import io.holunda.polyflow.taskpool.sender.gateway.CommandListGateway
import io.holunda.polyflow.taskpool.serialize
import mu.KLogging

/**
 * Simple sender for process variable commands.
 */
internal class SimpleProcessVariableCommandSender(
  private val commandListGateway: CommandListGateway,
  private val senderProperties: SenderProperties,
  private val objectMapper: ObjectMapper
) : ProcessVariableCommandSender {
  companion object : KLogging()

  override fun send(command: SingleProcessVariableCommand) {
    if (senderProperties.enabled && senderProperties.processVariable.enabled) {
      commandListGateway.sendToGateway(
        listOf(
          when (command) {
            is CreateSingleProcessVariableCommand -> ChangeProcessVariablesForExecutionCommand(
              sourceReference = command.sourceReference,
              variableChanges = listOf(
                ProcessVariableCreate(
                  value = command.value.serialize(objectMapper),
                  variableInstanceId = command.variableInstanceId,
                  variableName = command.variableName,
                  revision = command.revision,
                  scopeActivityInstanceId = command.scopeActivityInstanceId
                )
              )
            )
            is UpdateSingleProcessVariableCommand -> ChangeProcessVariablesForExecutionCommand(
              sourceReference = command.sourceReference,
              variableChanges = listOf(
                ProcessVariableUpdate(
                  value = command.value.serialize(objectMapper),
                  variableInstanceId = command.variableInstanceId,
                  variableName = command.variableName,
                  revision = command.revision,
                  scopeActivityInstanceId = command.scopeActivityInstanceId
                )
              )
            )
            is DeleteSingleProcessVariableCommand -> ChangeProcessVariablesForExecutionCommand(
              sourceReference = command.sourceReference,
              variableChanges = listOf(
                ProcessVariableDelete(
                  variableInstanceId = command.variableInstanceId,
                  variableName = command.variableName,
                  revision = command.revision,
                  scopeActivityInstanceId = command.scopeActivityInstanceId
                )
              )
            )
            else -> throw IllegalArgumentException("Unknown variable command received $command")
          }
        )
      )
    } else {
      logger.debug { "SENDER-009: Process variable sending is disabled by property. Would have sent $command." }
    }
  }
}


