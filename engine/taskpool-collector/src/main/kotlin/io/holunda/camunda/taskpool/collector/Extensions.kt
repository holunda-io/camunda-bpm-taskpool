package io.holunda.camunda.taskpool.collector

import io.holunda.camunda.taskpool.api.task.CaseReference
import io.holunda.camunda.taskpool.api.task.ProcessReference
import io.holunda.camunda.taskpool.api.task.SourceReference
import org.camunda.bpm.engine.delegate.DelegateTask

fun DelegateTask.sourceReference(applicationName: String): SourceReference =
  when {
    this.processDefinitionId != null -> ProcessReference(
      definitionId = this.processDefinitionId,
      instanceId = this.processInstanceId,
      executionId = this.executionId,
      definitionKey = this.processDefinitionKey(),
      name = this.processName(),
      applicationName = applicationName,
      tenantId = this.tenantId
    )
    this.caseDefinitionId != null -> CaseReference(
      definitionId = this.caseDefinitionId,
      instanceId = this.caseInstanceId,
      executionId = this.caseExecutionId,
      definitionKey = this.caseDefinitionKey(),
      name = this.caseName(),
      applicationName = applicationName,
      tenantId = this.tenantId
    )
    else -> throw IllegalArgumentException("No source reference found.")
  }


/**
 * Retrieves form key if found, or <code>null</code>.
 */
fun DelegateTask.formKey(): String? {
  val definitionId: String = when {
    processDefinitionId != null -> processDefinitionId
    caseDefinitionId != null -> caseDefinitionId
    else -> return null
  }
  return this.processEngine.formService.getTaskFormKey(definitionId, this.taskDefinitionKey)
}


fun DelegateTask.caseDefinitionKey(): String = caseDefinition().key
fun DelegateTask.caseName(): String = caseDefinition().name
fun DelegateTask.processDefinitionKey(): String = processDefinition().key
fun DelegateTask.processName(): String = processDefinition().name


fun DelegateTask.caseDefinition() = this.processEngine.repositoryService
  .createCaseDefinitionQuery().caseDefinitionId(caseDefinitionId)
  .singleResult()
  ?: throw IllegalArgumentException("Case definition could not be resolved for id $caseDefinitionId")

fun DelegateTask.processDefinition() = this.processEngine.repositoryService
  .createProcessDefinitionQuery().processDefinitionId(processDefinitionId)
  .singleResult()
  ?: throw IllegalArgumentException("Process definition could not be resolved for id $processDefinitionId")


