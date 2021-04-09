package io.holunda.camunda.taskpool.view.simple.service

import io.holixon.axon.gateway.query.QueryResponseMessageResponseType
import io.holixon.axon.gateway.query.RevisionValue
import io.holunda.camunda.taskpool.api.process.instance.*
import io.holunda.camunda.taskpool.api.process.variable.ProcessVariableCreate
import io.holunda.camunda.taskpool.api.process.variable.ProcessVariableDelete
import io.holunda.camunda.taskpool.api.process.variable.ProcessVariableUpdate
import io.holunda.camunda.taskpool.api.process.variable.ProcessVariablesChangedEvent
import io.holunda.camunda.taskpool.view.ProcessInstance
import io.holunda.camunda.taskpool.view.ProcessInstanceState
import io.holunda.camunda.taskpool.view.ProcessVariable
import io.holunda.camunda.taskpool.view.query.process.ProcessInstanceApi
import io.holunda.camunda.taskpool.view.query.process.ProcessInstanceQueryResult
import io.holunda.camunda.taskpool.view.query.process.ProcessInstancesByStateQuery
import io.holunda.camunda.taskpool.view.query.process.variable.ProcessVariableApi
import io.holunda.camunda.taskpool.view.query.process.variable.ProcessVariableQueryResult
import io.holunda.camunda.taskpool.view.query.process.variable.ProcessVariablesForInstanceQuery
import mu.KLogging
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.axonframework.messaging.MetaData
import org.axonframework.queryhandling.QueryHandler
import org.axonframework.queryhandling.QueryResponseMessage
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory process instance projection.
 */
@Component
@ProcessingGroup(SimpleServiceViewProcessingGroup.PROCESSING_GROUP)
class SimpleProcessInstanceService(
  val queryUpdateEmitter: QueryUpdateEmitter
) : ProcessInstanceApi, ProcessVariableApi {

  companion object : KLogging()

  private val revisionSupport = RevisionSupport()
  private val processInstances = ConcurrentHashMap<String, ProcessInstance>()
  private val processVariables = ConcurrentHashMap<String, MutableSet<ProcessVariable>>()

  /**
   * Query by the state.
   */
  @QueryHandler
  override fun query(query: ProcessInstancesByStateQuery): QueryResponseMessage<ProcessInstanceQueryResult> {
    val filtered = processInstances.values.filter { query.applyFilter(it) }
    return QueryResponseMessageResponseType.asQueryResponseMessage(
      payload = ProcessInstanceQueryResult(elements = filtered),
      metaData = revisionSupport.getRevisionMax(filtered.map { it.processInstanceId }).toMetaData()
    )
  }

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
   * Handles start of process instance.
   */
  @EventHandler
  fun on(event: ProcessInstanceStartedEvent, metaData: MetaData) {
    processInstances[event.processInstanceId] = event.toProcessInstance()
    revisionSupport.updateRevision(event.processInstanceId, RevisionValue.fromMetaData(metaData))
    logger.debug { "SIMPLE-VIEW-41: Process instance started $event." }
    updateProcessInstanceQuery(event.processInstanceId)
  }

  /**
   * Handles end of process instance.
   */
  @EventHandler
  fun on(event: ProcessInstanceEndedEvent, metaData: MetaData) {
    processInstances[event.processInstanceId] = event.toProcessInstance(processInstances[event.processInstanceId])
    revisionSupport.updateRevision(event.processInstanceId, RevisionValue.fromMetaData(metaData))
    logger.debug { "SIMPLE-VIEW-42: Process instance finished $event." }
    updateProcessInstanceQuery(event.processInstanceId)
  }

  /**
   * Handles suspend of process instance.
   */
  @EventHandler
  fun on(event: ProcessInstanceSuspendedEvent, metaData: MetaData) {
    val processInstance = processInstances[event.processInstanceId]
    if (processInstance != null) {
      processInstances[event.processInstanceId] = processInstance.copy(state = ProcessInstanceState.SUSPENDED)
      revisionSupport.updateRevision(event.processInstanceId, RevisionValue.fromMetaData(metaData))
      logger.debug { "SIMPLE-VIEW-43: Process instance suspended $event." }
      updateProcessInstanceQuery(event.processInstanceId)
    }
  }

  /**
   * Handles resume of process instance.
   */
  @EventHandler
  fun on(event: ProcessInstanceResumedEvent, metaData: MetaData) {
    val processInstance = processInstances[event.processInstanceId]
    if (processInstance != null) {
      processInstances[event.processInstanceId] = processInstance.copy(state = ProcessInstanceState.RUNNING)
      revisionSupport.updateRevision(event.processInstanceId, RevisionValue.fromMetaData(metaData))
      logger.debug { "SIMPLE-VIEW-44: Process instance resumed $event." }
      updateProcessInstanceQuery(event.processInstanceId)
    }
  }

  /**
   * Handles cancel of process instance.
   */
  @EventHandler
  fun on(event: ProcessInstanceCancelledEvent, metaData: MetaData) {
    processInstances[event.processInstanceId] = event.toProcessInstance(processInstances[event.processInstanceId])
    revisionSupport.updateRevision(event.processInstanceId, RevisionValue.fromMetaData(metaData))
    logger.debug { "SIMPLE-VIEW-45: Process instance was cancelled $event." }
    updateProcessInstanceQuery(event.processInstanceId)
  }

  @EventHandler
  fun on(event: ProcessVariablesChangedEvent, metaData: MetaData) {
    if (!processInstances.containsKey(event.sourceReference.instanceId)) {
      // received information about unknown process instance id.
      processInstances[event.sourceReference.instanceId] = event.toProcessInstance()
    }

    val toDelete = event.variableChanges.filterIsInstance<ProcessVariableDelete>().map { change -> change.variableInstanceId }
    processVariables.getOrDefault(event.sourceReference.instanceId, mutableListOf()).apply {
      this.removeIf { variable -> toDelete.contains(variable.variableInstanceId) }
      this.addAll(event.variableChanges.filterIsInstance<ProcessVariableCreate>().map { it.toProcessVariable(event.sourceReference) })
      this.addAll(event.variableChanges.filterIsInstance<ProcessVariableUpdate>().map { it.toProcessVariable(event.sourceReference) })
    }

    revisionSupport.updateRevision(event.sourceReference.instanceId, RevisionValue.fromMetaData(metaData))
    logger.debug { "SIMPLE-VIEW-46: Process instance variables has been updated $event." }
    updateProcessInstanceQuery(event.sourceReference.instanceId)
  }


  private fun updateProcessInstanceQuery(processInstanceId: String) {
    val revisionValue = revisionSupport.getRevisionMax(setOf(processInstanceId))
    logger.debug { "SIMPLE-VIEW-49: Updating query with new element $processInstanceId with revision $revisionValue" }
    val processInstance = processInstances.getValue(processInstanceId)
    queryUpdateEmitter.emit(
      ProcessInstancesByStateQuery::class.java,
      { query -> query.applyFilter(processInstance) },
      QueryResponseMessageResponseType.asSubscriptionUpdateMessage(
        payload = ProcessInstanceQueryResult(elements = listOf(processInstance)),
        metaData = revisionValue.toMetaData()
      )
    )
  }
}
