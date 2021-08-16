package io.holunda.polyflow.view.jpa

import com.fasterxml.jackson.databind.ObjectMapper
import io.holixon.axon.gateway.query.QueryResponseMessageResponseType
import io.holixon.axon.gateway.query.RevisionValue
import io.holunda.camunda.taskpool.api.business.DataEntryCreatedEvent
import io.holunda.camunda.taskpool.api.business.DataEntryUpdatedEvent
import io.holunda.polyflow.view.filter.Criterion
import io.holunda.polyflow.view.filter.toCriteria
import io.holunda.polyflow.view.jpa.JpaPolyflowViewService.Companion.PROCESSING_GROUP
import io.holunda.polyflow.view.jpa.data.*
import io.holunda.polyflow.view.jpa.data.AuthorizationPrincipal.Companion.group
import io.holunda.polyflow.view.jpa.data.AuthorizationPrincipal.Companion.user
import io.holunda.polyflow.view.jpa.data.DataEntryRepository.Companion.isAuthorizedFor
import io.holunda.polyflow.view.query.PageableSortableQuery
import io.holunda.polyflow.view.query.data.*
import mu.KLogging
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.axonframework.messaging.MetaData
import org.axonframework.queryhandling.QueryHandler
import org.axonframework.queryhandling.QueryResponseMessage
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
@ProcessingGroup(PROCESSING_GROUP)
class JpaPolyflowViewService(
  val dataEntryRepository: DataEntryRepository,
  val objectMapper: ObjectMapper,
  val queryUpdateEmitter: QueryUpdateEmitter,
  val polyflowJpaViewProperties: PolyflowJpaViewProperties
) : DataEntryApi {

  companion object : KLogging() {
    const val PROCESSING_GROUP = "io.holunda.polyflow.view.jpa.service"
  }

  @QueryHandler
  override fun query(query: DataEntryForIdentityQuery, metaData: MetaData): QueryResponseMessage<DataEntriesQueryResult> {
    val entryId = query.entryId
    require(entryId != null) { "Entry id must be set on query by id" }

    val elements = dataEntryRepository.findAllById(listOf(DataEntryId(entryId = entryId, entryType = query.entryType)))
    return constructResponse(elements, query)
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

    return constructResponse(elements, query)
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

    return constructResponse(elements, query)
  }

  /**
   * Constructs response message slicing it.
   */
  private fun constructResponse(
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

  /**
   * Creates new data entry.
   */
  @Suppress("unused")
  @EventHandler
  fun on(event: DataEntryCreatedEvent, metaData: MetaData) {
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
    updateDataEntryQuery(entity = entity)
  }


  /**
   * Updates data entry.
   */
  @Suppress("unused")
  @EventHandler
  fun on(event: DataEntryUpdatedEvent, metaData: MetaData) {
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
    updateDataEntryQuery(entity = entity)
  }

  private fun getMaxRevision(elementRevisions: List<Long>): RevisionValue =
    elementRevisions.maxByOrNull { it }?.let { RevisionValue(it) } ?: RevisionValue.NO_REVISION

  /**
   * Updates query for provided data entry identity.
   * @param entity entity to notify about.
   */
  private fun updateDataEntryQuery(entity: DataEntryEntity) {

    val entry = entity.toDataEntry(objectMapper)
    val revisionValue = RevisionValue(revision = entity.revision)

    logger.debug { "JPA-VIEW-43: Updating query with new element ${entry.identity} with revision $revisionValue" }

    queryUpdateEmitter.emit(
      DataEntriesForUserQuery::class.java,
      { query -> query.applyFilter(entry) },
      QueryResponseMessageResponseType.asSubscriptionUpdateMessage(
        payload = DataEntriesQueryResult(elements = listOf(entry)),
        metaData = revisionValue.toMetaData()
      )
    )
    queryUpdateEmitter.emit(
      DataEntryForIdentityQuery::class.java,
      { query -> query.applyFilter(entry) },
      QueryResponseMessageResponseType.asSubscriptionUpdateMessage(
        payload = DataEntriesQueryResult(elements = listOf(entry)),
        metaData = revisionValue.toMetaData()
      )
    )
    queryUpdateEmitter.emit(
      DataEntriesQuery::class.java,
      { query -> query.applyFilter(entry) },
      QueryResponseMessageResponseType.asSubscriptionUpdateMessage(
        payload = DataEntriesQueryResult(elements = listOf(entry)),
        metaData = revisionValue.toMetaData()
      )
    )
  }
}
