package io.holunda.camunda.taskpool.api.sender

import io.holunda.camunda.taskpool.api.task.ProcessDefinitionCommand

interface ProcessCommandSender {
  fun sendProcessCommand(command: ProcessDefinitionCommand)
}
