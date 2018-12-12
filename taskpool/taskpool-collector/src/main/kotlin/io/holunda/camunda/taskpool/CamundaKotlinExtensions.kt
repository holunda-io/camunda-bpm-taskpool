package io.holunda.camunda.taskpool

import org.camunda.bpm.engine.RepositoryService

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
