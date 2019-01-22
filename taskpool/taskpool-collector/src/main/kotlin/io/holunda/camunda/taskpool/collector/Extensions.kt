package io.holunda.camunda.taskpool.collector

import io.holunda.camunda.taskpool.api.task.CaseReference
import io.holunda.camunda.taskpool.api.task.ProcessReference
import io.holunda.camunda.taskpool.api.task.SourceReference
import io.holunda.camunda.taskpool.extractKey
import io.holunda.camunda.taskpool.loadCaseName
import io.holunda.camunda.taskpool.loadProcessName
import org.camunda.bpm.engine.FormService
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.delegate.DelegateTask

fun DelegateTask.sourceReference(repositoryService: RepositoryService, applicationName: String): SourceReference =
  when {
    this.processDefinitionId != null -> ProcessReference(
      definitionId = this.processDefinitionId,
      instanceId = this.processInstanceId,
      executionId = this.executionId,
      definitionKey = this.processDefinitionKey(),
      name = this.processName(repositoryService),
      applicationName = applicationName,
      tenantId = this.tenantId
    )
    this.caseDefinitionId != null -> CaseReference(
      definitionId = this.caseDefinitionId,
      instanceId = this.caseInstanceId,
      executionId = this.caseExecutionId,
      definitionKey = this.caseDefinitionKey(),
      name = this.caseName(repositoryService),
      applicationName = applicationName,
      tenantId = this.tenantId
    )
    else -> throw IllegalArgumentException("No source reference found.")
  }


fun DelegateTask.processDefinitionKey(): String = extractKey(this.processDefinitionId)
/**
 * Retrieves form key if found, or <code>null</code>.
 */
fun DelegateTask.formKey(formService: FormService): String? {
  val definitionId: String = when {
    processDefinitionId != null -> processDefinitionId
    caseDefinitionId != null -> caseDefinitionId
    else -> return null
  }
  return formService.getTaskFormKey(definitionId, this.taskDefinitionKey)
}

fun DelegateTask.caseDefinitionKey(): String = extractKey(this.caseDefinitionId)
fun DelegateTask.caseName(repositoryService: RepositoryService) = loadCaseName(this.caseDefinitionId, repositoryService)
fun DelegateTask.processName(repositoryService: RepositoryService) = loadProcessName(this.processDefinitionId, repositoryService)



