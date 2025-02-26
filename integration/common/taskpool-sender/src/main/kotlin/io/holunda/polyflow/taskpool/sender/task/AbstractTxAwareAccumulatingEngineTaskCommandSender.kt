package io.holunda.polyflow.taskpool.sender.task

import io.holunda.camunda.taskpool.api.task.EngineTaskCommand
import io.holunda.polyflow.taskpool.sender.SenderProperties
import io.holunda.polyflow.taskpool.sender.task.accumulator.EngineTaskCommandAccumulator
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager

/**
 * Collects commands of one transaction, accumulates them to one command and sends it after TX commit.
 */
abstract class AbstractTxAwareAccumulatingEngineTaskCommandSender(
  protected val engineTaskCommandAccumulator: EngineTaskCommandAccumulator,
  protected val senderProperties: SenderProperties
) : EngineTaskCommandSender {

  private val registered: ThreadLocal<Boolean> = ThreadLocal.withInitial { false }

  @Suppress("RemoveExplicitTypeArguments")
  protected val taskCommands: ThreadLocal<MutableMap<String, MutableList<EngineTaskCommand>>> =
    ThreadLocal.withInitial { mutableMapOf<String, MutableList<EngineTaskCommand>>() }

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

  /**
   * Triggers the command sending.
   */
  abstract fun send()
}
