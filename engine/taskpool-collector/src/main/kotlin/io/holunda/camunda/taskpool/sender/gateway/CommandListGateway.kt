package io.holunda.camunda.taskpool.sender.gateway

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
interface TaskCommandErrorHandler : BiFunction<Any, CommandResultMessage<out Any?>, Unit>

/**
 * Handler for command errors.
 */
interface TaskCommandSuccessHandler : BiFunction<Any, CommandResultMessage<out Any?>, Unit>
