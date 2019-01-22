package io.holunda.camunda.taskpool.sender.gateway

/**
 * Defines a gateway proxy, for sending commands.
 */
interface CommandListGateway {

  /**
   * Sends a list of commands to gateway.
   */
  fun sendToGateway(commands: List<Any>)

}
