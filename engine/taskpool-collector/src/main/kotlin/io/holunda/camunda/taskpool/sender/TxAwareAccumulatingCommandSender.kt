package io.holunda.camunda.taskpool.sender

import io.holunda.camunda.taskpool.api.task.EngineTaskCommand
import io.holunda.camunda.taskpool.sender.accumulator.CommandAccumulator
import io.holunda.camunda.taskpool.sender.gateway.CommandListGateway
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.transaction.support.TransactionSynchronizationAdapter
import org.springframework.transaction.support.TransactionSynchronizationManager

/**
 * Collects commands of one transaction, accumulates them to one command and sends it after TX commit.
 */
open class TxAwareAccumulatingCommandSender(
  private val commandListGateway: CommandListGateway,
  private val commandAccumulator: CommandAccumulator,
  private val sendTasksWithinTransaction: Boolean
) : CommandSender {
  private val logger: Logger = LoggerFactory.getLogger(CommandSender::class.java)

  private val registered: ThreadLocal<Boolean> = ThreadLocal.withInitial { false }
  @Suppress("RemoveExplicitTypeArguments")
  private val commands: ThreadLocal<MutableMap<String, MutableList<EngineTaskCommand>>> = ThreadLocal.withInitial { mutableMapOf<String, MutableList<EngineTaskCommand>>() }

  /**
   * Sends an engine command.send
   */
  override fun send(command: EngineTaskCommand) {

    // add command to list
    commands.get().getOrPut(command.id) { mutableListOf() }.add(command)

    // register synchronization only once
    if (!registered.get()) {
      // send the result

      TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronizationAdapter() {

        /**
         * Execute send if flag is set to send inside the TX.
         */
        override fun beforeCommit(readOnly: Boolean) {
          if (sendTasksWithinTransaction) {
            send()
          }
        }
        /**
         * Execute send if flag is set to send outside the TX.
         */
        override fun afterCommit() {
          if (!sendTasksWithinTransaction) {
            send()
          }
        }

        /**
         * Clean-up the thread on completion.
         */
        override fun afterCompletion(status: Int) {
          commands.remove()
          registered.remove()
        }
      })

      // mark as registered
      registered.set(true)
    }
  }

  /**
   * Send commands on commit only.
   */
  private fun send() {
    // iterate over messages and send them
    commands.get().forEach { (taskId: String, taskCommands: MutableList<EngineTaskCommand>) ->
      val accumulatorName = commandAccumulator::class.simpleName
      logger.debug("SENDER-005: Handling ${taskCommands.size} commands for task $taskId using command accumulator $accumulatorName")

      val commands = commandAccumulator.invoke(taskCommands)

      // handle messages for every task
      commandListGateway.sendToGateway(commands)
    }
  }

}
