package io.holunda.camunda.taskpool.sender

import io.holunda.camunda.taskpool.api.task.EngineTaskCommand
import io.holunda.camunda.taskpool.sender.accumulator.CommandAccumulator
import io.holunda.camunda.taskpool.sender.gateway.CommandListGateway
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.transaction.support.TransactionSynchronizationAdapter
import org.springframework.transaction.support.TransactionSynchronizationManager

/**
 * Collects commands asState one transaction, accumulates them to one command and sends it after TX commit.
 */
open class TxAwareAccumulatingCommandSender(
  private val commandListGateway: CommandListGateway,
  private val commandAccumulator: CommandAccumulator
) : CommandSender {
  private val logger: Logger = LoggerFactory.getLogger(CommandSender::class.java)

  private val registered: ThreadLocal<Boolean> = ThreadLocal.withInitial { false }
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
         * Send commands on commit only.
         */
        override fun afterCommit() {
          // iterate over messages and send them
          commands.get().forEach { (taskId: String, taskCommands: MutableList<EngineTaskCommand>) ->
            val accumulatorName = commandAccumulator::class.simpleName
            logger.debug("SENDER-005: Handling ${taskCommands.size} commands for task $taskId using command accumulator $accumulatorName")

            val commands = commandAccumulator.invoke(taskCommands)

            // handle messages for every task
            commandListGateway.sendToGateway(commands)
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
}
