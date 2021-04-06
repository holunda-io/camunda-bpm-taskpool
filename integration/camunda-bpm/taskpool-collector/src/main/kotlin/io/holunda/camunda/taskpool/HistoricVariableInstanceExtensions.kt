package io.holunda.camunda.taskpool

import io.holunda.camunda.taskpool.api.task.CaseReference
import io.holunda.camunda.taskpool.api.task.ProcessReference
import io.holunda.camunda.taskpool.api.task.SourceReference
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.impl.history.event.HistoricVariableUpdateEventEntity
import org.camunda.bpm.engine.repository.CaseDefinition
import org.camunda.bpm.engine.repository.ProcessDefinition

/**
 * Extracts source reference out of historic variable event.
 */
fun HistoricVariableUpdateEventEntity.sourceReference(repositoryService: RepositoryService, applicationName: String): SourceReference =
  when {
    this.processDefinitionId != null -> ProcessReference(
      definitionId = this.processDefinitionId,
      instanceId = this.processInstanceId,
      executionId = this.executionId,
      definitionKey = this.processDefinitionKey,
      name = this.processName(repositoryService),
      applicationName = applicationName,
      tenantId = this.tenantId
    )
    this.caseDefinitionId != null -> CaseReference(
      definitionId = this.caseDefinitionId,
      instanceId = this.caseInstanceId,
      executionId = this.caseExecutionId,
      definitionKey = this.caseDefinitionKey,
      name = this.caseName(repositoryService),
      applicationName = applicationName,
      tenantId = this.tenantId
    )
    else -> throw IllegalArgumentException("No source reference found.")
  }

/**
 * Retrieves case definition key from event.
 * @param repositoryService to resolve definition
 */
fun HistoricVariableUpdateEventEntity.caseDefinitionKey(repositoryService: RepositoryService): String = caseDefinition(repositoryService).key

/**
 * Retrieves case name from event. If the case name is not set, fall back to key.
 * @param repositoryService to resolve definition.
 */
fun HistoricVariableUpdateEventEntity.caseName(repositoryService: RepositoryService): String = caseDefinition(repositoryService).name
  ?: caseDefinitionKey(repositoryService)

/**
 * Retrieves process definition key from event.
 * @param repositoryService to resolve definition
 */
fun HistoricVariableUpdateEventEntity.processDefinitionKey(repositoryService: RepositoryService): String = processDefinition(repositoryService).key

/**
 * Retrieves process name from event. If the process name is not set, fall back to key.
 * @param repositoryService to resolve definition
 */
fun HistoricVariableUpdateEventEntity.processName(repositoryService: RepositoryService): String = processDefinition(repositoryService).name
  ?: processDefinitionKey(repositoryService)

/**
 * Retrieves case definition from event.
 * @param repositoryService to resolve definition
 */
fun HistoricVariableUpdateEventEntity.caseDefinition(repositoryService: RepositoryService): CaseDefinition = repositoryService
  .createCaseDefinitionQuery().caseDefinitionId(caseDefinitionId)
  .singleResult()
  ?: throw IllegalArgumentException("Case definition could not be resolved for id $caseDefinitionId")

/**
 * Retrieves process definition from event.
 * @param repositoryService to resolve definition
 */
fun HistoricVariableUpdateEventEntity.processDefinition(repositoryService: RepositoryService): ProcessDefinition = repositoryService
  .createProcessDefinitionQuery().processDefinitionId(processDefinitionId)
  .singleResult()
  ?: throw IllegalArgumentException("Process definition could not be resolved for id $processDefinitionId")

