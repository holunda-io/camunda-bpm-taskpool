package io.holunda.camunda.taskpool

import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import org.camunda.bpm.engine.impl.interceptor.Command
import org.camunda.bpm.engine.impl.persistence.entity.IdentityLinkEntity
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity

fun extractKey(processDefinitionId: String?): String {
  if (processDefinitionId == null) {
    throw IllegalArgumentException("Process definition id must not be null.")
  }
  return processDefinitionId.split(regex = ":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
}

fun loadProcessName(processDefinitionId: String, repositoryService: RepositoryService): String {
  val processDefinition = repositoryService.createProcessDefinitionQuery()
    .processDefinitionId(processDefinitionId)
    .singleResult()
    ?: throw IllegalArgumentException("Process definition could not be resolved for id $processDefinitionId")
  return processDefinition.name
}

fun loadCaseName(caseDefinitionId: String, repositoryService: RepositoryService): String {
  val caseDefinition = repositoryService.createCaseDefinitionQuery()
    .caseDefinitionId(caseDefinitionId)
    .singleResult()
    ?: throw IllegalArgumentException("Case definition could not be resolved for id $caseDefinitionId")
  return caseDefinition.name
}

/**
 * Runs a command in command context.
 */
fun <T> ProcessEngineConfigurationImpl.executeInCommandContext(command: Command<T>): T {
  return this.commandExecutorTxRequired.execute(command)
}

fun ProcessDefinitionEntity.candidateLinks(): List<IdentityLinkEntity> = this.identityLinks.filter { it.type == "candidate" }
/**
 * Retrieves a set of candidate user ids allowed to start given process definition.
 */
fun ProcessDefinitionEntity.candidateUsers() = this.candidateLinks().filter { it.isUser }.map { it.userId }.toSet()
/**
 * Retrieves a set of candidate group ids allowed to start given process definition.
 */
fun ProcessDefinitionEntity.candidateGroups() = this.candidateLinks().filter { it.isGroup }.map { it.groupId }.toSet()

