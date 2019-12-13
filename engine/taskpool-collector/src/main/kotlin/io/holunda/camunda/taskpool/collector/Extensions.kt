package io.holunda.camunda.taskpool.collector

import io.holunda.camunda.taskpool.api.task.CaseReference
import io.holunda.camunda.taskpool.api.task.ProcessReference
import io.holunda.camunda.taskpool.api.task.SourceReference
import org.camunda.bpm.engine.delegate.DelegateTask

/**
 * Retrieves source reference (process or case) from delegate task.
 */
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

/**
 * Retrieves case definition key from delegate task.
 */
fun DelegateTask.caseDefinitionKey(): String = caseDefinition().key

/**
 * Retrieves case name from delegate task.
 */
fun DelegateTask.caseName(): String = caseDefinition().name ?: caseDefinitionKey()

/**
 * Retrieves process definition key from delegate task.
 */
fun DelegateTask.processDefinitionKey(): String = processDefinition().key

/**
 * Retrieves process name from delegate task. If the process name is not set, fall back to key.
 */
fun DelegateTask.processName(): String = processDefinition().name ?: processDefinitionKey()

/**
 * Retrieves case definition from delegate task.
 */
fun DelegateTask.caseDefinition() = this.processEngine.repositoryService
  .createCaseDefinitionQuery().caseDefinitionId(caseDefinitionId)
  .singleResult()
  ?: throw IllegalArgumentException("Case definition could not be resolved for id $caseDefinitionId")

/**
 * Retrieves process definition from delegate task.
 */
fun DelegateTask.processDefinition() = this.processEngine.repositoryService
  .createProcessDefinitionQuery().processDefinitionId(processDefinitionId)
  .singleResult()
  ?: throw IllegalArgumentException("Process definition could not be resolved for id $processDefinitionId")


