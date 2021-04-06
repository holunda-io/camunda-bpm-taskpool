package io.holunda.camunda.taskpool

import io.holunda.camunda.taskpool.api.task.CaseReference
import io.holunda.camunda.taskpool.api.task.ProcessReference
import io.holunda.camunda.taskpool.api.task.SourceReference
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.impl.history.event.HistoricProcessInstanceEventEntity
import org.camunda.bpm.engine.repository.CaseDefinition
import org.camunda.bpm.engine.repository.ProcessDefinition

/**
 * Retrieves source reference (process or case) from historic process instance.
 * @param applicationName name of the application.
 * @param repositoryService for definition resolution
 */
fun HistoricProcessInstanceEventEntity.sourceReference(repositoryService: RepositoryService, applicationName: String): SourceReference =
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
fun HistoricProcessInstanceEventEntity.caseDefinitionKey(repositoryService: RepositoryService): String = caseDefinition(repositoryService).key

/**
 * Retrieves case name from event. If the case name is not set, fall back to key.
 * @param repositoryService to resolve definition.
 */
fun HistoricProcessInstanceEventEntity.caseName(repositoryService: RepositoryService): String = caseDefinition(repositoryService).name
  ?: caseDefinitionKey(repositoryService)

/**
 * Retrieves process definition key from event.
 * @param repositoryService to resolve definition
 */
fun HistoricProcessInstanceEventEntity.processDefinitionKey(repositoryService: RepositoryService): String = processDefinition(repositoryService).key

/**
 * Retrieves process name from event. If the process name is not set, fall back to key.
 * @param repositoryService to resolve definition
 */
fun HistoricProcessInstanceEventEntity.processName(repositoryService: RepositoryService): String = processDefinition(repositoryService).name
  ?: processDefinitionKey(repositoryService)

/**
 * Retrieves case definition from event.
 * @param repositoryService to resolve definition
 */
fun HistoricProcessInstanceEventEntity.caseDefinition(repositoryService: RepositoryService): CaseDefinition = repositoryService
  .createCaseDefinitionQuery().caseDefinitionId(caseDefinitionId)
  .singleResult()
  ?: throw IllegalArgumentException("Case definition could not be resolved for id $caseDefinitionId")

/**
 * Retrieves process definition from event.
 * @param repositoryService to resolve definition
 */
fun HistoricProcessInstanceEventEntity.processDefinition(repositoryService: RepositoryService): ProcessDefinition = repositoryService
  .createProcessDefinitionQuery().processDefinitionId(processDefinitionId)
  .singleResult()
  ?: throw IllegalArgumentException("Process definition could not be resolved for id $processDefinitionId")
