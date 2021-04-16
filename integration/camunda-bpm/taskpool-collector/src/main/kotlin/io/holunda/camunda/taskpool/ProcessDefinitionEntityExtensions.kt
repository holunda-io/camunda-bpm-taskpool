package io.holunda.camunda.taskpool

import io.holunda.camunda.taskpool.api.process.definition.RegisterProcessDefinitionCommand
import org.camunda.bpm.engine.impl.persistence.entity.IdentityLinkEntity
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity

/**
 * Retrieves a set of candidate links allowed to start given process definition.
 */
fun ProcessDefinitionEntity.candidateLinks(): List<IdentityLinkEntity> = this.identityLinks.filter { it.type == "candidate" }

/**
 * Retrieves a set of candidate user ids allowed to start given process definition.
 */
fun ProcessDefinitionEntity.candidateUsers() = this.candidateLinks().filter { it.isUser }.map { it.userId }.toSet()

/**
 * Retrieves a set of candidate group ids allowed to start given process definition.
 */
fun ProcessDefinitionEntity.candidateGroups() = this.candidateLinks().filter { it.isGroup }.map { it.groupId }.toSet()

/**
 * Create a register process definition command for given application name and an optional form key.
 * @param applicationName name of the application.
 * @param formKey optional start form key.
 * @return command.
 */
fun ProcessDefinitionEntity.asCommand(applicationName: String, formKey: String?) =
  RegisterProcessDefinitionCommand(
    processDefinitionId = this.id,
    processDefinitionKey = this.key,
    processDefinitionVersion = this.version,
    processName = this.name ?: this.key,
    processVersionTag = this.versionTag,
    processDescription = this.description,
    startableFromTasklist = this.isStartableInTasklist,
    applicationName = applicationName,
    formKey = formKey,
    candidateStarterUsers = this.candidateUsers(),
    candidateStarterGroups = this.candidateGroups()
  )
