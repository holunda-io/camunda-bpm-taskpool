package io.holunda.polyflow.view.jpa

import com.fasterxml.jackson.databind.ObjectMapper
import io.holixon.axon.gateway.query.QueryResponseMessageResponseType
import io.holixon.axon.gateway.query.RevisionValue
import io.holunda.camunda.taskpool.api.business.DataEntryCreatedEvent
import io.holunda.camunda.taskpool.api.business.DataEntryUpdatedEvent
import io.holunda.polyflow.view.jpa.JpaPolyflowViewService.Companion.PROCESSING_GROUP
import io.holunda.polyflow.view.jpa.auth.AuthorizationPrincipal
import io.holunda.polyflow.view.jpa.auth.AuthorizationPrincipalRepository
import io.holunda.polyflow.view.jpa.data.DataEntryEntity
import io.holunda.polyflow.view.jpa.data.DataEntryId
import io.holunda.polyflow.view.jpa.data.DataEntryRepository
import io.holunda.polyflow.view.jpa.data.toEntity
import io.holunda.polyflow.view.jpa.data.toDataEntry
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
  val authorizationPrincipalRepository: AuthorizationPrincipalRepository,
  val objectMapper: ObjectMapper,
  val queryUpdateEmitter: QueryUpdateEmitter
) : DataEntryApi {

  companion object : KLogging() {
    const val PROCESSING_GROUP = "io.holunda.polyflow.view.jpa.service"
  }

  @QueryHandler
  override fun query(query: DataEntryForIdentityQuery, metaData: MetaData): QueryResponseMessage<DataEntriesQueryResult> {
    val entryId = query.entryId
    require(entryId != null) { "Entry id must be set on query by id" }

    val elements = dataEntryRepository.findAllById(listOf(DataEntryId(entryId = entryId, entryType = query.entryType)))
    val payload = DataEntriesQueryResult(elements = elements.map { it.toDataEntry(objectMapper) }).slice(query = query)

    return QueryResponseMessageResponseType.asQueryResponseMessage(
      payload = payload,
      metaData = getMaxRevision(elements.filter { dataEntryEntity ->
        payload.elements.map { dataEntry -> dataEntry.entryType to dataEntry.entryId }
          .contains(dataEntryEntity.dataEntryId.entryType to dataEntryEntity.dataEntryId.entryId)
      }.map { it.revision }).toMetaData()
    )
  }

  @QueryHandler
  override fun query(query: DataEntriesForUserQuery, metaData: MetaData): QueryResponseMessage<DataEntriesQueryResult> {

    // FIXME, construct query based on the filters.

    val authorizationQuery = setOf(
      AuthorizationPrincipal.user(query.user.username),
    ).plus(query.user.groups.map { AuthorizationPrincipal.group(it) })

    val elements = dataEntryRepository.findAllByAuthorizedPrincipalsIn(authorizationQuery)
    val payload = DataEntriesQueryResult(elements = elements.map { it.toDataEntry(objectMapper) }).slice(query = query)

    return QueryResponseMessageResponseType.asQueryResponseMessage(
      payload = payload,
      metaData = getMaxRevision(elements.filter { dataEntryEntity ->
        payload.elements.map { dataEntry -> dataEntry.entryType to dataEntry.entryId }
          .contains(dataEntryEntity.dataEntryId.entryType to dataEntryEntity.dataEntryId.entryId)
      }.map { it.revision }).toMetaData()
    )

  }

  @QueryHandler
  override fun query(query: DataEntriesQuery, metaData: MetaData): QueryResponseMessage<DataEntriesQueryResult> {

    // FIXME, construct query based on the filters.

    val elements = dataEntryRepository.findAll()
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
    val entity = event.toEntity(objectMapper, RevisionValue.fromMetaData(metaData))
    // make sure authorizations exist
    authorizationPrincipalRepository.saveAll(entity.authorizedPrincipals)
    logger.debug { "JPA-VIEW-41: Business data entry created $event." }
    dataEntryRepository.save(entity)
    updateDataEntryQuery(entity = entity)
  }


  /**
   * Updates data entry.
   */
  @Suppress("unused")
  @EventHandler
  fun on(event: DataEntryUpdatedEvent, metaData: MetaData) {
    val savedEntity = dataEntryRepository.findByIdOrNull(DataEntryId(entryType = event.entryType, entryId = event.entryId))
    val entity = event.toEntity(objectMapper, RevisionValue.fromMetaData(metaData), oldEntry = savedEntity)
    dataEntryRepository.save(entity)
    logger.debug { "JPA-VIEW-42: Business data entry updated $event" }
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
