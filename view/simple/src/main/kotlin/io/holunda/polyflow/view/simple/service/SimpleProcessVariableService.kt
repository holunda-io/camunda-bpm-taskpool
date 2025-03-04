package io.holunda.polyflow.view.simple.service

import io.github.oshai.kotlinlogging.KotlinLogging
import io.holixon.axon.gateway.query.QueryResponseMessageResponseType
import io.holixon.axon.gateway.query.RevisionValue
import io.holunda.camunda.taskpool.api.process.variable.ProcessVariableCreate
import io.holunda.camunda.taskpool.api.process.variable.ProcessVariableDelete
import io.holunda.camunda.taskpool.api.process.variable.ProcessVariableUpdate
import io.holunda.camunda.taskpool.api.process.variable.ProcessVariablesChangedEvent
import io.holunda.polyflow.view.ProcessVariable
import io.holunda.polyflow.view.query.process.variable.ProcessVariableApi
import io.holunda.polyflow.view.query.process.variable.ProcessVariableQueryResult
import io.holunda.polyflow.view.query.process.variable.ProcessVariablesForInstanceQuery
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.axonframework.messaging.MetaData
import org.axonframework.queryhandling.QueryHandler
import org.axonframework.queryhandling.QueryResponseMessage
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

private val logger = KotlinLogging.logger {}

/**
 * In-memory process instance projection.
 */
@Component
@ProcessingGroup(SimpleServiceViewProcessingGroup.PROCESSING_GROUP)
class SimpleProcessVariableService(
  private val queryUpdateEmitter: QueryUpdateEmitter,
  private val revisionSupport: RevisionSupport = RevisionSupport(),
  private val processVariables: ConcurrentHashMap<String, MutableSet<ProcessVariable>> = ConcurrentHashMap<String, MutableSet<ProcessVariable>>()
) : ProcessVariableApi {

  /**
   * Query for process variables of a process instance.
   */
  @QueryHandler
  override fun query(query: ProcessVariablesForInstanceQuery): QueryResponseMessage<ProcessVariableQueryResult> {
    val processInstanceVariables = processVariables.getOrDefault(query.processInstanceId, emptyList())
    val filtered = processInstanceVariables.filter { query.applyFilter(it) }
    return QueryResponseMessageResponseType.asQueryResponseMessage(
      payload = ProcessVariableQueryResult(variables = filtered),
      metaData = revisionSupport.getRevisionMax(filtered.map { it.sourceReference.instanceId }).toMetaData()
    )
  }


  /**
   * Handles process variable change event.
   */
  @EventHandler
  fun on(event: ProcessVariablesChangedEvent, metaData: MetaData) {

    val toDelete = event.variableChanges.filterIsInstance<ProcessVariableDelete>().map { change -> change.variableInstanceId }
    processVariables.getOrDefault(event.sourceReference.instanceId, mutableListOf()).apply {
      this.removeIf { variable -> toDelete.contains(variable.variableInstanceId) }
      this.addAll(event.variableChanges.filterIsInstance<ProcessVariableCreate>().map { it.toProcessVariable(event.sourceReference) })
      this.addAll(event.variableChanges.filterIsInstance<ProcessVariableUpdate>().map { it.toProcessVariable(event.sourceReference) })
    }

    revisionSupport.updateRevision(event.sourceReference.instanceId, RevisionValue.fromMetaData(metaData))
    logger.debug { "SIMPLE-VIEW-46: Process instance variables has been updated $event." }
    updateProcessVariableQuery(event.sourceReference.instanceId)
  }

  /**
   * Read-only stored data.
   */
  fun getProcessVariables(): Map<String, Set<ProcessVariable>> = processVariables.toMap()

  private fun updateProcessVariableQuery(processInstanceId: String) {
    if (processVariables.contains(processInstanceId)) {
      val processVariablesForInstance = processVariables.getValue(processInstanceId)
      queryUpdateEmitter.emit(
        ProcessVariablesForInstanceQuery::class.java,
        // send update if at least one variable of the queried is being modified
        { query -> processVariablesForInstance.any { variable -> query.applyFilter(variable) } },
        processVariablesForInstance
      )
    }
  }
}
