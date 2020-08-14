package io.holunda.camunda.taskpool.collector

import io.holunda.camunda.taskpool.api.task.CaseReference
import io.holunda.camunda.taskpool.api.task.ProcessReference
import io.holunda.camunda.taskpool.api.task.SourceReference
import org.camunda.bpm.engine.impl.history.event.HistoricProcessInstanceEventEntity

/**
 * Retrieves source reference (process or case) from historic process instance.
 * @param applicationName name of the application.
 */
fun HistoricProcessInstanceEventEntity.sourceReference(applicationName: String): SourceReference =
  when {
    this.processDefinitionId != null -> ProcessReference(
      definitionId = this.processDefinitionId,
      instanceId = this.processInstanceId,
      executionId = this.executionId,
      definitionKey = this.processDefinitionKey,
      name = this.processDefinitionName,
      applicationName = applicationName,
      tenantId = this.tenantId
    )
    this.caseDefinitionId != null -> CaseReference(
      definitionId = this.caseDefinitionId,
      instanceId = this.caseInstanceId,
      executionId = this.caseExecutionId,
      definitionKey = this.caseDefinitionKey,
      name = this.caseDefinitionName,
      applicationName = applicationName,
      tenantId = this.tenantId
    )
    else -> throw IllegalArgumentException("No source reference found.")
  }

