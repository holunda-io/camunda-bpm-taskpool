package io.holunda.polyflow.view.jpa

import com.fasterxml.jackson.databind.ObjectMapper
import io.holixon.axon.gateway.query.QueryResponseMessageResponseType
import io.holixon.axon.gateway.query.RevisionValue
import io.holunda.camunda.taskpool.api.business.DataEntryCreatedEvent
import io.holunda.camunda.taskpool.api.business.DataEntryUpdatedEvent
import io.holunda.camunda.taskpool.api.process.definition.ProcessDefinitionRegisteredEvent
import io.holunda.camunda.taskpool.api.process.instance.*
import io.holunda.polyflow.view.ProcessDefinition
import io.holunda.polyflow.view.filter.Criterion
import io.holunda.polyflow.view.filter.toCriteria
import io.holunda.polyflow.view.jpa.JpaPolyflowViewService.Companion.PROCESSING_GROUP
import io.holunda.polyflow.view.jpa.auth.AuthorizationPrincipal
import io.holunda.polyflow.view.jpa.auth.AuthorizationPrincipal.Companion.group
import io.holunda.polyflow.view.jpa.auth.AuthorizationPrincipal.Companion.user
import io.holunda.polyflow.view.jpa.data.*
import io.holunda.polyflow.view.jpa.data.DataEntryRepository.Companion.isAuthorizedFor
import io.holunda.polyflow.view.jpa.process.*
import io.holunda.polyflow.view.jpa.process.ProcessDefinitionRepository.Companion.isStarterAuthorizedFor
import io.holunda.polyflow.view.jpa.process.ProcessInstanceRepository.Companion.hasStates
import io.holunda.polyflow.view.jpa.update.TxAwareQueryUpdateEmitter
import io.holunda.polyflow.view.query.PageableSortableQuery
import io.holunda.polyflow.view.query.data.*
import io.holunda.polyflow.view.query.process.*
import mu.KLogging
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.axonframework.messaging.MetaData
import org.axonframework.queryhandling.QueryHandler
import org.axonframework.queryhandling.QueryResponseMessage
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

/**
 * Implementation of the Polyflow View API using JPA to create the persistence model.
 */
@Component
@ProcessingGroup(PROCESSING_GROUP)
class JpaPolyflowViewService(
  val dataEntryRepository: DataEntryRepository,
  val processInstanceRepository: ProcessInstanceRepository,
  val processDefinitionRepository: ProcessDefinitionRepository,
  val objectMapper: ObjectMapper,
  val txAwareQueryUpdateEmitter: TxAwareQueryUpdateEmitter,
  val polyflowJpaViewProperties: PolyflowJpaViewProperties
) : DataEntryApi, DataEntryEventHandler, ProcessInstanceApi, ProcessDefinitionApi {

  companion object : KLogging() {
    const val PROCESSING_GROUP = "io.holunda.polyflow.view.jpa.service"
  }

  @QueryHandler
  override fun query(query: DataEntryForIdentityQuery, metaData: MetaData): QueryResponseMessage<DataEntriesQueryResult> {
    val entryId = query.entryId
    require(entryId != null) { "Entry id must be set on query by id" }

    val elements = dataEntryRepository.findAllById(listOf(DataEntryId(entryId = entryId, entryType = query.entryType)))
    return constructDataEntryResponse(elements, query)
  }

  @QueryHandler
  override fun query(query: DataEntriesForUserQuery, metaData: MetaData): QueryResponseMessage<DataEntriesQueryResult> {

    val authorizedPrincipals: Set<AuthorizationPrincipal> = setOf(user(query.user.username)).plus(query.user.groups.map { group(it) })
    val criteria: List<Criterion> = toCriteria(query.filters)
    val specification = criteria.toSpecification()

    val elements = if (specification != null) {
      dataEntryRepository.findAll(specification.and(isAuthorizedFor(authorizedPrincipals)))
    } else {
      dataEntryRepository.findAll(isAuthorizedFor(authorizedPrincipals))
    }

    return constructDataEntryResponse(elements, query)
  }

  @QueryHandler
  override fun query(query: DataEntriesQuery, metaData: MetaData): QueryResponseMessage<DataEntriesQueryResult> {

    val criteria: List<Criterion> = toCriteria(query.filters)
    val specification = criteria.toSpecification()
    val elements = if (specification != null) {
      dataEntryRepository.findAll(specification)
    } else {
      dataEntryRepository.findAll()
    }

    return constructDataEntryResponse(elements, query)
  }

  @QueryHandler
  override fun query(query: ProcessDefinitionsStartableByUserQuery): List<ProcessDefinition> {
    val authorizedPrincipals: Set<AuthorizationPrincipal> = setOf(user(query.user.username)).plus(query.user.groups.map { group(it) })
    return processDefinitionRepository.findAll(isStarterAuthorizedFor(authorizedPrincipals)).map { it.toProcessDefinition() }
  }

  @QueryHandler
  override fun query(query: ProcessInstancesByStateQuery): QueryResponseMessage<ProcessInstanceQueryResult> {
    return QueryResponseMessageResponseType.asQueryResponseMessage(
      payload = ProcessInstanceQueryResult(
        elements = processInstanceRepository.findAll(hasStates(query.states)).map { it.toProcessInstance() }
      )
    )
  }

  @Suppress("unused")
  @EventHandler
  override fun on(event: DataEntryCreatedEvent, metaData: MetaData) {
    val savedEntity = dataEntryRepository.findByIdOrNull(DataEntryId(entryType = event.entryType, entryId = event.entryId))
    val entity = if (savedEntity == null || savedEntity.lastModifiedDate < event.createModification.time.toInstant()) {
      /*
       * save the entity only if there is no newer entity in the database (possibly written by another instance of this service in HA setup)
       */
      dataEntryRepository.save(
        event.toEntity(
          objectMapper = objectMapper,
          revisionValue = RevisionValue.fromMetaData(metaData),
          limit = polyflowJpaViewProperties.payloadAttributeLevelLimit
        )
      ).apply {
        logger.debug { "JPA-VIEW-41: Business data entry created $event." }
      }
    } else {
      savedEntity
    }
    txAwareQueryUpdateEmitter.emitEntityUpdate(entity)
  }

  @Suppress("unused")
  @EventHandler
  override fun on(event: DataEntryUpdatedEvent, metaData: MetaData) {
    val savedEntity = dataEntryRepository.findByIdOrNull(DataEntryId(entryType = event.entryType, entryId = event.entryId))
    val entity = if (savedEntity == null || savedEntity.lastModifiedDate < event.updateModification.time.toInstant()) {
      /*
       * save the entity only if there is no newer entity in the database (possibly written by another instance of this service in HA setup)
       */
      dataEntryRepository.save(
        event.toEntity(
          objectMapper = objectMapper,
          revisionValue = RevisionValue.fromMetaData(metaData),
          oldEntry = savedEntity,
          limit = polyflowJpaViewProperties.payloadAttributeLevelLimit
        )
      ).apply {
        logger.debug { "JPA-VIEW-42: Business data entry updated $event" }
      }
    } else {
      savedEntity
    }
    txAwareQueryUpdateEmitter.emitEntityUpdate(entity)
  }

  /**
   * Registers a new process definition.
   */
  @Suppress("unused")
  @EventHandler
  fun on(event: ProcessDefinitionRegisteredEvent) {
    val entity = processDefinitionRepository.save(
      event.toEntity()
    )
    txAwareQueryUpdateEmitter.emitEntityUpdate(entity)
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
    txAwareQueryUpdateEmitter.emitEntityUpdate(entity)
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
      txAwareQueryUpdateEmitter.emitEntityUpdate(entity)
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
      txAwareQueryUpdateEmitter.emitEntityUpdate(entity)
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
      txAwareQueryUpdateEmitter.emitEntityUpdate(entity)
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
      txAwareQueryUpdateEmitter.emitEntityUpdate(entity)
    }
  }

  /**
   * Constructs response message slicing it.
   */
  private fun constructDataEntryResponse(
    elements: Iterable<DataEntryEntity>,
    query: PageableSortableQuery
  ): QueryResponseMessage<DataEntriesQueryResult> {

    val payload = DataEntriesQueryResult(elements = elements.map { it.toDataEntry(objectMapper) }).slice(query = query)

    return QueryResponseMessageResponseType.asQueryResponseMessage(
      payload = payload,
      metaData = getMaxRevision(elements.filter { dataEntryEntity ->
        payload.elements.map { dataEntry -> dataEntry.entryType to dataEntry.entryId }
          .contains(dataEntryEntity.dataEntryId.entryType to dataEntryEntity.dataEntryId.entryId)
      }.map { it.revision }).toMetaData()
    )
  }


  private fun getMaxRevision(elementRevisions: List<Long>): RevisionValue =
    elementRevisions.maxByOrNull { it }?.let { RevisionValue(it) } ?: RevisionValue.NO_REVISION

}
