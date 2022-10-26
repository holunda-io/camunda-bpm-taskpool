package io.holunda.camunda.taskpool.mapper.process

import io.holunda.camunda.taskpool.api.process.definition.ProcessDefinitionRegisteredEvent
import io.holunda.camunda.taskpool.api.process.definition.RegisterProcessDefinitionCommand

fun RegisterProcessDefinitionCommand.registerEvent(): ProcessDefinitionRegisteredEvent =
  ProcessDefinitionRegisteredEvent(
    processDefinitionId = this.processDefinitionId,
    processDefinitionKey = this.processDefinitionKey,
    processDefinitionVersion = this.processDefinitionVersion,
    processDescription = this.processDescription,
    processName = this.processName,
    processVersionTag = this.processVersionTag,
    applicationName = this.applicationName,
    candidateStarterGroups = this.candidateStarterGroups,
    candidateStarterUsers = this.candidateStarterUsers,
    formKey = this.formKey,
    startableFromTasklist = this.startableFromTasklist
  )