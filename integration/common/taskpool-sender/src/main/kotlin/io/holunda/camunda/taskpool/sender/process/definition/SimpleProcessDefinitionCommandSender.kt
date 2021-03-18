package io.holunda.camunda.taskpool.sender.process.definition

import io.holunda.camunda.taskpool.api.process.definition.ProcessDefinitionCommand
import io.holunda.camunda.taskpool.sender.gateway.CommandListGateway

class SimpleProcessDefinitionCommandSender(commandListGateway: CommandListGateway) : ProcessDefinitionCommandSender {

  override fun send(command: ProcessDefinitionCommand) {
    TODO("Not yet implemented")
  }
}
