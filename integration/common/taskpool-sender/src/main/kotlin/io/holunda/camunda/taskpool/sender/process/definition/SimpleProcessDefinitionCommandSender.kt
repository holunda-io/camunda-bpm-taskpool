package io.holunda.camunda.taskpool.sender.process.definition

import io.holunda.camunda.taskpool.api.process.definition.ProcessDefinitionCommand
import io.holunda.camunda.taskpool.sender.gateway.CommandListGateway

/**
 * Simple sender for process definition commands
 */
class SimpleProcessDefinitionCommandSender(
  private val commandListGateway: CommandListGateway
) : ProcessDefinitionCommandSender {

  override fun send(command: ProcessDefinitionCommand) {
    commandListGateway.sendToGateway(listOf(command))
  }
}
