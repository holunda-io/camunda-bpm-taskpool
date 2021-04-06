package io.holunda.camunda.taskpool.sender.task

import io.holunda.camunda.taskpool.api.task.EngineTaskCommand
import io.holunda.camunda.taskpool.sender.SenderProperties
import io.holunda.camunda.taskpool.sender.gateway.CommandListGateway
import io.holunda.camunda.taskpool.sender.task.accumulator.EngineTaskCommandAccumulator
import mu.KLogging
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager

/**
 * Collects commands of one transaction, accumulates them to one command and sends it after TX commit.
 */
internal class TxAwareAccumulatingEngineTaskCommandSender(
  private val commandListGateway: CommandListGateway,
  private val engineTaskCommandAccumulator: EngineTaskCommandAccumulator,
  private val senderProperties: SenderProperties
) : EngineTaskCommandSender {

  companion object: KLogging()

  private val registered: ThreadLocal<Boolean> = ThreadLocal.withInitial { false }
  @Suppress("RemoveExplicitTypeArguments")
  private val taskCommands: ThreadLocal<MutableMap<String, MutableList<EngineTaskCommand>>> = ThreadLocal.withInitial { mutableMapOf<String, MutableList<EngineTaskCommand>>() }

  /**
   * Sends an engine task command (after commit).
   */
  override fun send(command: EngineTaskCommand) {

    // add command to list
    taskCommands.get().getOrPut(command.id) { mutableListOf() }.add(command)

    // register synchronization only once
    if (!registered.get()) {
      // send the result

      TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {

        /**
         * Execute send if flag is set to send inside the TX.
         */
        override fun beforeCommit(readOnly: Boolean) {
          if (senderProperties.task.sendWithinTransaction) {
            send()
          }
        }

        /**
         * Execute send if flag is set to send outside the TX.
         */
        override fun afterCommit() {
          if (!senderProperties.task.sendWithinTransaction) {
            send()
          }
        }

        /**
         * Clean-up the thread on completion.
         */
        override fun afterCompletion(status: Int) {
          taskCommands.remove()
          registered.remove()
        }
      })

      // mark as registered
      registered.set(true)
    }
  }

  private fun send() {
    // iterate over messages and send them
    taskCommands.get().forEach { (taskId: String, taskCommands: MutableList<EngineTaskCommand>) ->
      val accumulatorName = engineTaskCommandAccumulator::class.simpleName
      logger.debug("SENDER-005: Handling ${taskCommands.size} commands for task $taskId using command accumulator $accumulatorName")
      val commands = engineTaskCommandAccumulator.invoke(taskCommands)
      // handle messages for every task
      if (senderProperties.enabled) {
        commandListGateway.sendToGateway(commands)
      } else {
        logger.debug { "SENDER-004: Process task sending is disabled by property. Would have sent $commands." }
      }
    }
  }

}
