package io.holunda.polyflow.datapool.sender

import com.fasterxml.jackson.databind.ObjectMapper
import io.holunda.polyflow.datapool.DataEntrySenderProperties
import io.holunda.polyflow.datapool.projector.DataEntryProjector
import mu.KLogging
import org.axonframework.commandhandling.CommandMessage
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager

/**
 * Collects commands of one transaction, accumulates them to one command and sends it after TX commit.
 */
abstract class TxAwareAccumulatingDataEntryCommandSender(
  properties: DataEntrySenderProperties,
  dataEntryProjector: DataEntryProjector,
  objectMapper: ObjectMapper
) : AbstractDataEntryCommandSender(properties, dataEntryProjector, objectMapper) {

  /** Logger instance for this class. */
  companion object : KLogging()

  private val registered: ThreadLocal<Boolean> = ThreadLocal.withInitial { false }

  protected val dataEntryCommands: ThreadLocal<MutableMap<String, MutableList<CommandMessage<*>>>> =
    ThreadLocal.withInitial { mutableMapOf() }

  override fun <C> send(command: CommandMessage<C>) {
    // add command to list
    dataEntryCommands.get().getOrPut(command.identifier) { mutableListOf() }.add(command)

    // register synchronization only once
    if (!registered.get()) {
      // send the result

      TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {

        /**
         * Execute send if flag is set to send inside the TX.
         */
        override fun beforeCommit(readOnly: Boolean) {
          if (properties.sendWithinTransaction) {
            send()
          }
        }

        /**
         * Execute send if flag is set to send outside the TX.
         */
        override fun afterCommit() {
          if (!properties.sendWithinTransaction) {
            send()
          }
        }

        /**
         * Clean-up the thread on completion.
         */
        override fun afterCompletion(status: Int) {
          dataEntryCommands.remove()
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
