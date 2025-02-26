package io.holunda.polyflow.view.simple.service

import io.github.oshai.kotlinlogging.KotlinLogging
import io.holixon.axon.gateway.query.QueryResponseMessageResponseType
import io.holixon.axon.gateway.query.RevisionValue
import io.holunda.camunda.taskpool.api.process.instance.*
import io.holunda.polyflow.view.ProcessInstance
import io.holunda.polyflow.view.ProcessInstanceState
import io.holunda.polyflow.view.query.process.ProcessInstanceApi
import io.holunda.polyflow.view.query.process.ProcessInstanceQueryResult
import io.holunda.polyflow.view.query.process.ProcessInstancesByStateQuery
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
class SimpleProcessInstanceService(
  private val queryUpdateEmitter: QueryUpdateEmitter,
  private val revisionSupport: RevisionSupport = RevisionSupport(),
  private val processInstances: ConcurrentHashMap<String, ProcessInstance> = ConcurrentHashMap<String, ProcessInstance>()
) : ProcessInstanceApi {

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

  /**
   * Read-only stored data.
   */
  fun getProcessInstances(): Map<String, ProcessInstance> = processInstances.toMap()

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
