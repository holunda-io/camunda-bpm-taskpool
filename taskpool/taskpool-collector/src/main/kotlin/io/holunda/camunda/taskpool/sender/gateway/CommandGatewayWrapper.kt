package io.holunda.camunda.taskpool.sender.gateway

import io.holunda.camunda.taskpool.api.task.WithTaskId

/**
 * Definies a gateway proxy, for sending commands.
 */
interface CommandGatewayWrapper {

  /**
   * Sends a list of commands to gateway.
   */
  fun sendToGateway(commands: List<WithTaskId>)

  /**
   * Sends a single command to gateway.
   */
  fun sendToGateway(command: WithTaskId) = sendToGateway(listOf(command))
}
