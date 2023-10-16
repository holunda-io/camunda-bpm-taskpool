package io.holunda.polyflow.view.jpa

import com.fasterxml.jackson.databind.ObjectMapper
import io.holixon.axon.gateway.query.QueryResponseMessageResponseType
import io.holixon.axon.gateway.query.RevisionValue
import io.holunda.camunda.taskpool.api.business.DataEntryCreatedEvent
import io.holunda.camunda.taskpool.api.business.DataEntryDeletedEvent
import io.holunda.camunda.taskpool.api.business.DataEntryUpdatedEvent
import io.holunda.polyflow.view.DataEntry
import io.holunda.polyflow.view.filter.Criterion
import io.holunda.polyflow.view.filter.toCriteria
import io.holunda.polyflow.view.jpa.JpaPolyflowViewDataEntryService.Companion.PROCESSING_GROUP
import io.holunda.polyflow.view.jpa.auth.AuthorizationPrincipal
import io.holunda.polyflow.view.jpa.auth.AuthorizationPrincipal.Companion.group
import io.holunda.polyflow.view.jpa.auth.AuthorizationPrincipal.Companion.user
import io.holunda.polyflow.view.jpa.data.*
import io.holunda.polyflow.view.jpa.data.DataEntryRepository.Companion.hasEntryId
import io.holunda.polyflow.view.jpa.data.DataEntryRepository.Companion.hasEntryType
import io.holunda.polyflow.view.jpa.data.DataEntryRepository.Companion.isAuthorizedFor
import io.holunda.polyflow.view.jpa.update.updateDataEntryQuery
import io.holunda.polyflow.view.query.PageableSortableQuery
import io.holunda.polyflow.view.query.data.*
import mu.KLogging
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.axonframework.messaging.MetaData
import org.axonframework.queryhandling.QueryHandler
import org.axonframework.queryhandling.QueryResponseMessage
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.springframework.data.domain.Page
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

/**
 * Implementation of the Polyflow Data Entry View API using JPA to create the persistence model.
 */
@Component
@ProcessingGroup(PROCESSING_GROUP)
class JpaPolyflowViewDataEntryService(
  val dataEntryRepository: DataEntryRepository,
  val objectMapper: ObjectMapper,
  val queryUpdateEmitter: QueryUpdateEmitter,
  val polyflowJpaViewProperties: PolyflowJpaViewProperties
) : DataEntryApi, DataEntryEventHandler {

  companion object : KLogging() {
    const val PROCESSING_GROUP = "io.holunda.polyflow.view.jpa.service.data"
  }

  @QueryHandler
  override fun query(query: DataEntryForIdentityQuery, metaData: MetaData): QueryResponseMessage<DataEntry> {
    val specification = composeAnd(listOf(hasEntryId(query.entryId), hasEntryType(query.entryType)))
    return dataEntryRepository.findOne(specification).map {
      QueryResponseMessageResponseType.asQueryResponseMessage<DataEntry>(
        payload = it.toDataEntry(objectMapper),
        metaData = RevisionValue(it.revision).toMetaData()
      )
    }.orElse(
      QueryResponseMessageResponseType.asQueryResponseMessage(
        payload = null,
        metaData = MetaData.emptyInstance()
      )
    )
  }

  @QueryHandler
  override fun query(query: DataEntriesForDataEntryTypeQuery, metaData: MetaData): QueryResponseMessage<DataEntriesQueryResult> {
    val specification = hasEntryType(query.entryType)
    val pageRequest = pageRequest(query.page, query.size, query.sort)
    val page = dataEntryRepository.findAll(specification, pageRequest)
    return constructDataEntryResponse(page)
  }

  @QueryHandler
  override fun query(query: DataEntriesForUserQuery, metaData: MetaData): QueryResponseMessage<DataEntriesQueryResult> {

    val authorizedPrincipals: Set<AuthorizationPrincipal> = setOf(user(query.user.username)).plus(query.user.groups.map { group(it) })
    val criteria: List<Criterion> = toCriteria(query.filters)
    val specification = criteria.toDataEntrySpecification()
    val pageRequest = pageRequest(query.page, query.size, query.sort)

    val page = dataEntryRepository.findAll(specification.and(isAuthorizedFor(authorizedPrincipals)), pageRequest)
    return constructDataEntryResponse(page)
  }

  @QueryHandler
  override fun query(query: DataEntriesQuery, metaData: MetaData): QueryResponseMessage<DataEntriesQueryResult> {

    val criteria: List<Criterion> = toCriteria(query.filters)
    val specification = criteria.toDataEntrySpecification()
    val pageRequest = pageRequest(query.page, query.size, query.sort)

    val page = dataEntryRepository.findAll(specification, pageRequest)
    return constructDataEntryResponse(page)
  }

  @Suppress("unused")
  @EventHandler
  override fun on(event: DataEntryCreatedEvent, metaData: MetaData) {
    if (isDisabledByProperty()) return

    val savedEntity = dataEntryRepository.findByIdOrNull(DataEntryId(entryType = event.entryType, entryId = event.entryId))
    val entity = if (savedEntity == null || savedEntity.lastModifiedDate < event.createModification.time.toInstant()) {
      /*
       * save the entity only if there is no newer entity in the database (possibly written by another instance of this service in HA setup)
       */
      dataEntryRepository.save(
        event.toEntity(
          objectMapper = objectMapper,
          revisionValue = RevisionValue.fromMetaData(metaData),
          limit = polyflowJpaViewProperties.payloadAttributeLevelLimit,
          filters = polyflowJpaViewProperties.dataEntryJsonPathFilters()
        )
      ).apply {
        logger.debug { "JPA-VIEW-41: Business data entry created $event." }
      }
    } else {
      savedEntity
    }
    emitDataEntryUpdate(entity)
  }

  @Suppress("unused")
  @EventHandler
  override fun on(event: DataEntryUpdatedEvent, metaData: MetaData) {
    if (isDisabledByProperty()) return

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
          limit = polyflowJpaViewProperties.payloadAttributeLevelLimit,
          filters = polyflowJpaViewProperties.dataEntryJsonPathFilters()
        )
      ).apply {
        logger.debug { "JPA-VIEW-42: Business data entry updated $event" }
      }
    } else {
      savedEntity
    }
    emitDataEntryUpdate(entity)
  }

  @Suppress("unused")
  @EventHandler
  override fun on(event: DataEntryDeletedEvent, metaData: MetaData) {
    if (isDisabledByProperty()) return

    val identity = DataEntryId(entryType = event.entryType, entryId = event.entryId)
    val existingEntity = dataEntryRepository.findByIdOrNull(identity)
    if (existingEntity != null) {
      dataEntryRepository.deleteById(identity)
      logger.debug { "JPA-VIEW-43: Business data entry deleted $event" }
    } else {
      logger.warn { "JPA-VIEW-44: Business data entry was already deleted. Ignored a duplicate event $event" }
    }
  }

  /**
   * Constructs response message slicing it.
   */
  private fun constructDataEntryResponse(
    page: Page<DataEntryEntity>
  ): QueryResponseMessage<DataEntriesQueryResult> {

    val payload = DataEntriesQueryResult(
      elements = page.map { it.toDataEntry(objectMapper) }.distinct().toList(),
      totalElementCount = page.totalElements.toInt()
    )

    return QueryResponseMessageResponseType.asQueryResponseMessage(
      payload = payload,
      metaData = getMaxRevision(page.toList().filter { dataEntryEntity ->
        payload.elements.map { dataEntry -> dataEntry.entryType to dataEntry.entryId }
          .contains(dataEntryEntity.dataEntryId.entryType to dataEntryEntity.dataEntryId.entryId)
      }.map { it.revision }).toMetaData()
    )
  }

  private fun getMaxRevision(elementRevisions: List<Long>): RevisionValue =
    elementRevisions.maxByOrNull { it }?.let { RevisionValue(it) } ?: RevisionValue.NO_REVISION

  private fun emitDataEntryUpdate(entity: DataEntryEntity) {
    queryUpdateEmitter.updateDataEntryQuery(entity.toDataEntry(objectMapper), RevisionValue(entity.revision))
  }

  private fun reportMissingFeature(query: PageableSortableQuery) {
    if (query.sort.isEmpty()) {
      logger.warn { "Sorting is currently not supported, but the sort was requested: ${query.sort}, see https://github.com/holunda-io/camunda-bpm-taskpool/issues/701" }
    }
  }


  private fun isDisabledByProperty(): Boolean {
    return (!polyflowJpaViewProperties.storedItems.contains(StoredItem.DATA_ENTRY)).also {
      if (it) {
        logger.debug { "Data entry storage disabled by property." }
      }
    }
  }
}
