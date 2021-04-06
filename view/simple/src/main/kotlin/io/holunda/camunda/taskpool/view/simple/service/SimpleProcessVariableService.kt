package io.holunda.camunda.taskpool.view.simple.service

import io.holixon.axon.gateway.query.RevisionValue
import io.holunda.camunda.taskpool.api.process.variable.ProcessVariableCreatedEvent
import io.holunda.camunda.taskpool.api.process.variable.ProcessVariableDeletedEvent
import io.holunda.camunda.taskpool.api.process.variable.ProcessVariableUpdatedEvent
import io.holunda.camunda.taskpool.view.ProcessVariable
import mu.KLogging
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.axonframework.messaging.MetaData
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

/**
 * Process variable in-memory projection.
 */
@Component
@ProcessingGroup(SimpleServiceViewProcessingGroup.PROCESSING_GROUP)
class SimpleProcessVariableService(
  val queryUpdateEmitter: QueryUpdateEmitter
) {

  companion object : KLogging()

  private val revisionSupport = RevisionSupport()
  private val processVariables = ConcurrentHashMap<String, ProcessVariable>()


  @EventHandler
  fun on(event: ProcessVariableCreatedEvent, metaData: MetaData) {
    logger.info { "SIMPLE-VIEW-31: Process Variable created event $event received" }
    processVariables[event.variableInstanceId] = event.toProcessVariable()
    revisionSupport.updateRevision(event.variableInstanceId, RevisionValue.fromMetaData(metaData))
  }

  @EventHandler
  fun on(event: ProcessVariableUpdatedEvent, metaData: MetaData) {
    logger.info { "SIMPLE-VIEW-32: Process Variable updated event $event received" }
    processVariables[event.variableInstanceId] = event.toProcessVariable(processVariables[event.variableInstanceId])
    revisionSupport.updateRevision(event.variableInstanceId, RevisionValue.fromMetaData(metaData))
  }

  @EventHandler
  fun on(event: ProcessVariableDeletedEvent, metaData: MetaData) {
    logger.info { "SIMPLE-VIEW-31: Process Variable deleted event $event received" }
    processVariables.remove(event.variableInstanceId)
    revisionSupport.updateRevision(event.variableInstanceId, RevisionValue.fromMetaData(metaData))
  }
}
