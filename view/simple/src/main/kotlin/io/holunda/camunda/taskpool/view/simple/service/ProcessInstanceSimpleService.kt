package io.holunda.camunda.taskpool.view.simple.service

import io.holixon.axon.gateway.query.QueryResponseMessageResponseType
import io.holixon.axon.gateway.query.RevisionValue
import io.holunda.camunda.taskpool.api.process.instance.*
import io.holunda.camunda.taskpool.view.ProcessInstance
import io.holunda.camunda.taskpool.view.ProcessInstanceState
import io.holunda.camunda.taskpool.view.query.process.ProcessInstanceApi
import io.holunda.camunda.taskpool.view.query.process.ProcessInstanceQueryResult
import io.holunda.camunda.taskpool.view.query.process.ProcessInstancesByStateQuery
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
class ProcessInstanceSimpleService(
  val queryUpdateEmitter: QueryUpdateEmitter
) : ProcessInstanceApi {

  companion object : KLogging()

  private val revisionSupport = RevisionSupport()
  private val processInstances = ConcurrentHashMap<String, ProcessInstance>()

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

/**
 * Converts event to view model.
 */
fun ProcessInstanceStartedEvent.toProcessInstance(): ProcessInstance = ProcessInstance(
  processInstanceId = this.processInstanceId,
  sourceReference = this.sourceReference,
  businessKey = this.businessKey,
  superInstanceId = this.superInstanceId,
  startActivityId = this.startActivityId,
  startUserId = this.startUserId,
  state = ProcessInstanceState.RUNNING
)

/**
 * Converts event to view model.
 * @param processInstance an old version of the view model.
 */
fun ProcessInstanceEndedEvent.toProcessInstance(processInstance: ProcessInstance?): ProcessInstance = ProcessInstance(
  processInstanceId = this.processInstanceId,
  sourceReference = this.sourceReference,
  businessKey = this.businessKey,
  superInstanceId = this.superInstanceId,
  endActivityId = this.endActivityId,
  startActivityId = processInstance?.startActivityId,
  startUserId = processInstance?.startUserId,
  state = ProcessInstanceState.FINISHED
)

/**
 * Converts event to view model.
 * @param processInstance an old version of the view model.
 */
fun ProcessInstanceCancelledEvent.toProcessInstance(processInstance: ProcessInstance?): ProcessInstance = ProcessInstance(
  processInstanceId = this.processInstanceId,
  sourceReference = this.sourceReference,
  businessKey = this.businessKey,
  superInstanceId = this.superInstanceId,
  endActivityId = this.endActivityId,
  deleteReason = this.deleteReason,
  startActivityId = processInstance?.startActivityId,
  startUserId = processInstance?.startUserId,
  state = ProcessInstanceState.CANCELLED
)
