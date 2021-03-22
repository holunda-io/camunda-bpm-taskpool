package io.holunda.camunda.taskpool.sender.process.instance

import io.holunda.camunda.taskpool.api.process.definition.ProcessDefinitionCommand
import io.holunda.camunda.taskpool.sender.gateway.CommandListGateway

/**
 * Simple sender for process definition commands
 */
class SimpleProcessInstanceCommandSender(
  private val commandListGateway: CommandListGateway
) : ProcessInstanceCommandSender {

  override fun send(command: ProcessDefinitionCommand) {
    commandListGateway.sendToGateway(listOf(command))
  }
}
