package io.holunda.polyflow.datapool.sender

import org.axonframework.commandhandling.CommandMessage

/**
 * Interface for beans sending data entry commands.
 */
interface DataEntrySender {
  /**
   * Sends the command to core and enriches it, if possible.
   */
  fun <C> send(command: CommandMessage<C>)
}
