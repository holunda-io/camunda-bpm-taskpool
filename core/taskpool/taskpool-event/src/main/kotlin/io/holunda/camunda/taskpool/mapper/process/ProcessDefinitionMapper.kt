package io.holunda.camunda.taskpool.mapper.process

import io.holunda.camunda.taskpool.api.process.definition.ProcessDefinitionRegisteredEvent
import io.holunda.camunda.taskpool.api.process.definition.ProcessDefinitionSuspendedEvent
import io.holunda.camunda.taskpool.api.process.definition.ProcessDefinitionActivatedEvent
import io.holunda.camunda.taskpool.api.process.definition.ProcessDefinitionUnregisteredEvent
import io.holunda.camunda.taskpool.api.process.definition.SuspendProcessDefinitionCommand
import io.holunda.camunda.taskpool.api.process.definition.ActivateProcessDefinitionCommand
import io.holunda.camunda.taskpool.api.process.definition.UnregisterProcessDefinitionCommand
import io.holunda.camunda.taskpool.api.process.definition.RegisterProcessDefinitionCommand

/**
 * Maps command to event.
 */
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
    startableFromTasklist = this.startableFromTasklist,
    payload = this.payload,
  )

/**
 * Maps a suspension command to its event.
 */
fun SuspendProcessDefinitionCommand.suspendEvent() = ProcessDefinitionSuspendedEvent(processDefinitionId, payload)

/**
 * Maps an activation command to its event.
 */
fun ActivateProcessDefinitionCommand.activateEvent() = ProcessDefinitionActivatedEvent(processDefinitionId, payload)

/**
 * Maps an unregistration command to its event.
 */
fun UnregisterProcessDefinitionCommand.unregisterEvent() = ProcessDefinitionUnregisteredEvent(processDefinitionId, payload)
