package io.holunda.polyflow.view.jpa

import io.holixon.axon.gateway.query.QueryResponseMessageResponseType
import io.holunda.camunda.taskpool.api.process.instance.*
import io.holunda.polyflow.view.jpa.JpaPolyflowViewProcessInstanceService.Companion.PROCESSING_GROUP
import io.holunda.polyflow.view.jpa.process.*
import io.holunda.polyflow.view.jpa.process.ProcessInstanceRepository.Companion.hasStates
import io.holunda.polyflow.view.query.process.ProcessInstanceApi
import io.holunda.polyflow.view.query.process.ProcessInstanceQueryResult
import io.holunda.polyflow.view.query.process.ProcessInstancesByStateQuery
import mu.KLogging
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.axonframework.queryhandling.QueryHandler
import org.axonframework.queryhandling.QueryResponseMessage
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.springframework.stereotype.Component

/**
 * Implementation of the Polyflow Process Instance View API using JPA to create the persistence model.
 */
@Component
@ProcessingGroup(PROCESSING_GROUP)
class JpaPolyflowViewProcessInstanceService(
  val processInstanceRepository: ProcessInstanceRepository,
  val queryUpdateEmitter: QueryUpdateEmitter,
) : ProcessInstanceApi {

  companion object : KLogging() {
    const val PROCESSING_GROUP = "io.holunda.polyflow.view.jpa.service.process.instance"
  }

  @QueryHandler
  override fun query(query: ProcessInstancesByStateQuery): QueryResponseMessage<ProcessInstanceQueryResult> {
    return QueryResponseMessageResponseType.asQueryResponseMessage(
      payload = ProcessInstanceQueryResult(
        elements = processInstanceRepository.findAll(hasStates(query.states)).map { it.toProcessInstance() }
      )
    )
  }

  /**
   * New instance started.
   */
  @Suppress("unused")
  @EventHandler
  fun on(event: ProcessInstanceStartedEvent) {
    val entity = processInstanceRepository.save(
      event.toEntity()
    )
    emitProcessInstanceUpdate(entity)
  }

  /**
   * Instance cancelled.
   */
  @Suppress("unused")
  @EventHandler
  fun on(event: ProcessInstanceCancelledEvent) {
    processInstanceRepository.findById(event.processInstanceId).ifPresent {
      val entity = processInstanceRepository.save(
        it.cancelInstance(event)
      )
      emitProcessInstanceUpdate(entity)
    }
  }

  /**
   * Instance suspended.
   */
  @Suppress("unused")
  @EventHandler
  fun on(event: ProcessInstanceSuspendedEvent) {
    processInstanceRepository.findById(event.processInstanceId).ifPresent {
      val entity = processInstanceRepository.save(
        it.suspendInstance()
      )
      emitProcessInstanceUpdate(entity)
    }
  }

  /**
   * Instance resumed.
   */
  @Suppress("unused")
  @EventHandler
  fun on(event: ProcessInstanceResumedEvent) {
    processInstanceRepository.findById(event.processInstanceId).ifPresent {
      val entity = processInstanceRepository.save(
        it.resumeInstance()
      )
      emitProcessInstanceUpdate(entity)
    }
  }

  /**
   * Instance ended.
   */
  @Suppress("unused")
  @EventHandler
  fun on(event: ProcessInstanceEndedEvent) {
    processInstanceRepository.findById(event.processInstanceId).ifPresent {
      val entity = processInstanceRepository.save(
        it.finishInstance(event)
      )
      emitProcessInstanceUpdate(entity)
    }
  }

  private fun emitProcessInstanceUpdate(entity: ProcessInstanceEntity) {
    queryUpdateEmitter.updateProcessInstanceQuery(entity.toProcessInstance())
  }

}
