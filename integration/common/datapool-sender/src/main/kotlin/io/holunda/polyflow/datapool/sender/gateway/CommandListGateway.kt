package io.holunda.polyflow.datapool.sender.gateway

import org.axonframework.commandhandling.CommandResultMessage
import java.util.function.BiFunction

/**
 * Defines a gateway proxy, for sending commands.
 */
interface CommandListGateway {

  /**
   * Sends a list of commands to gateway.
   */
  fun sendToGateway(commands: List<Any>)

}

/**
 * Handler for command errors.
 */
interface CommandErrorHandler : BiFunction<Any, CommandResultMessage<out Any?>, Unit>

/**
 * Handler for command results.
 */
interface CommandSuccessHandler : BiFunction<Any, CommandResultMessage<out Any?>, Unit>
