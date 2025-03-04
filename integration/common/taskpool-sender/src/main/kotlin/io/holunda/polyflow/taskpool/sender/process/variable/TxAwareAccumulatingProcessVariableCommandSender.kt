package io.holunda.polyflow.taskpool.sender.process.variable

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import io.holunda.camunda.taskpool.api.process.variable.ChangeProcessVariablesForExecutionCommand
import io.holunda.camunda.taskpool.api.process.variable.ProcessVariableCreate
import io.holunda.camunda.taskpool.api.process.variable.ProcessVariableDelete
import io.holunda.camunda.taskpool.api.process.variable.ProcessVariableUpdate
import io.holunda.polyflow.taskpool.sender.SenderProperties
import io.holunda.polyflow.taskpool.sender.gateway.CommandListGateway
import io.holunda.polyflow.taskpool.serialize
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager

private val logger = KotlinLogging.logger {}

/**
 * This sender collects all variable updates during one transaction and groups them by the
 * process instance id and sends over as a single command.
 */
class TxAwareAccumulatingProcessVariableCommandSender(
  private val commandListGateway: CommandListGateway,
  private val senderProperties: SenderProperties,
  private val objectMapper: ObjectMapper
) : ProcessVariableCommandSender {

  private val registered: ThreadLocal<Boolean> = ThreadLocal.withInitial { false }

  @Suppress("RemoveExplicitTypeArguments")
  private val singleProcessVariablesCommands: ThreadLocal<MutableMap<String, MutableList<SingleProcessVariableCommand>>> =
    ThreadLocal.withInitial { mutableMapOf<String, MutableList<SingleProcessVariableCommand>>() }

  override fun send(command: SingleProcessVariableCommand) {
    // add command to list
    singleProcessVariablesCommands.get().getOrPut(command.sourceReference.instanceId) { mutableListOf() }.add(command)

    // register synchronization only once
    if (!registered.get()) {

      TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {
        /**
         * Execute send if flag is set to send inside the TX.
         */
        override fun beforeCommit(readOnly: Boolean) {
          if (senderProperties.processVariable.sendWithinTransaction) {
            send()
          }
        }

        /**
         * Execute send if flag is set to send outside the TX.
         */
        override fun afterCommit() {
          if (!senderProperties.processVariable.sendWithinTransaction) {
            send()
          }
        }

        /**
         * Clean-up the thread on completion.
         */
        override fun afterCompletion(status: Int) {
          singleProcessVariablesCommands.remove()
          registered.remove()
        }
      })
      // mark as registered
      registered.set(true)
    }
  }

  private fun send() {
    singleProcessVariablesCommands.get().forEach { (_, commandList) -> // grouped by process instance id
      val commands = commandList
        .groupBy { command -> command.sourceReference.executionId } // group by execution id
        .map { commandsForOneExecution -> // build a change process variable command for every execution
          ChangeProcessVariablesForExecutionCommand(
            sourceReference = commandsForOneExecution.value.first().sourceReference,
            variableChanges = commandsForOneExecution.value.map { command ->
              when (command) {
                is CreateSingleProcessVariableCommand -> ProcessVariableCreate(
                  value = command.value.serialize(objectMapper),
                  variableInstanceId = command.variableInstanceId,
                  variableName = command.variableName,
                  revision = command.revision,
                  scopeActivityInstanceId = command.scopeActivityInstanceId
                )

                is UpdateSingleProcessVariableCommand -> ProcessVariableUpdate(
                  value = command.value.serialize(objectMapper),
                  variableInstanceId = command.variableInstanceId,
                  variableName = command.variableName,
                  revision = command.revision,
                  scopeActivityInstanceId = command.scopeActivityInstanceId
                )

                is DeleteSingleProcessVariableCommand -> ProcessVariableDelete(
                  variableInstanceId = command.variableInstanceId,
                  variableName = command.variableName,
                  revision = command.revision,
                  scopeActivityInstanceId = command.scopeActivityInstanceId
                )

                else -> throw IllegalArgumentException("Unsupported variable sender command.")
              }
            }
          )
        }

      if (senderProperties.enabled && senderProperties.processVariable.enabled) {
        commandListGateway.sendToGateway(commands)
      } else {
        logger.debug { "SENDER-009: Process variable sending is disabled by property. Would have sent $commands." }
      }

    }
  }

}
