package io.holunda.polyflow.view.simple.service

import io.holixon.axon.gateway.query.QueryResponseMessageResponseType
import io.holixon.axon.gateway.query.RevisionValue
import io.holunda.camunda.taskpool.api.business.DataEntryCreatedEvent
import io.holunda.camunda.taskpool.api.business.DataEntryUpdatedEvent
import io.holunda.camunda.taskpool.api.business.dataIdentityString
import io.holunda.polyflow.view.DataEntry
import io.holunda.polyflow.view.query.data.*
import io.holunda.polyflow.view.simple.filter.createDataEntryPredicates
import io.holunda.polyflow.view.simple.filter.filterByPredicate
import io.holunda.polyflow.view.simple.filter.toCriteria
import io.holunda.polyflow.view.simple.sort.dataComparator
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
 * Data entry in-memory projection.
 */
@Component
@ProcessingGroup(SimpleServiceViewProcessingGroup.PROCESSING_GROUP)
class SimpleDataEntryService(
  private val queryUpdateEmitter: QueryUpdateEmitter
) : DataEntryApi {

  companion object : KLogging()

  private val revisionSupport = RevisionSupport()
  private val dataEntries = ConcurrentHashMap<String, DataEntry>()

  /**
   * Creates new data entry.
   */
  @Suppress("unused")
  @EventHandler
  fun on(event: DataEntryCreatedEvent, metaData: MetaData) {

    val entryId = dataIdentityString(entryType = event.entryType, entryId = event.entryId)
    dataEntries[entryId] = event.toDataEntry()
    revisionSupport.updateRevision(entryId, RevisionValue.fromMetaData(metaData))
    logger.debug { "SIMPLE-VIEW-31: Business data entry created $event." }
    updateDataEntryQuery(entryId)
  }


  /**
   * Updates data entry.
   */
  @Suppress("unused")
  @EventHandler
  fun on(event: DataEntryUpdatedEvent, metaData: MetaData) {

    logger.debug { "SIMPLE-VIEW-32: Business data entry updated $event" }
    val entryId = dataIdentityString(entryType = event.entryType, entryId = event.entryId)
    dataEntries[entryId] = event.toDataEntry(dataEntries[entryId])
    revisionSupport.updateRevision(entryId, RevisionValue.fromMetaData(metaData))
    updateDataEntryQuery(entryId)
  }

  /**
   * Retrieves a list of all data entries of given entry type (and optional id).
   */
  @QueryHandler
  override fun query(query: DataEntriesQuery, metaData: MetaData): QueryResponseMessage<DataEntriesQueryResult> {
    val predicate = createDataEntryPredicates(toCriteria(query.filters))
    val filtered = dataEntries.values.filter { filterByPredicate(it, predicate) }
    val comparator = dataComparator(query.sort)
    val sorted = if (comparator != null) {
      filtered.sortedWith(comparator)
    } else {
      filtered
    }
    return QueryResponseMessageResponseType.asQueryResponseMessage(
      payload = DataEntriesQueryResult(elements = sorted).slice(query = query),
      metaData = revisionSupport.getRevisionMax(sorted.map { it.identity }).toMetaData()
    )
  }


  /**
   * Retrieves a list of all data entries of given entry type (and optional id).
   */
  @QueryHandler
  override fun query(query: DataEntryForIdentityQuery, metaData: MetaData): QueryResponseMessage<DataEntriesQueryResult> {
    val filtered = dataEntries.values.filter { query.applyFilter(it) }
    return QueryResponseMessageResponseType.asQueryResponseMessage(
      payload = DataEntriesQueryResult(elements = filtered),
      metaData = revisionSupport.getRevisionMax(filtered.map { it.identity }).toMetaData()
    )
  }

  /**
   * Retrieves a list of all data entries visible for current user matching the filter.
   */
  @QueryHandler
  override fun query(query: DataEntriesForUserQuery, metaData: MetaData): QueryResponseMessage<DataEntriesQueryResult> {

    val predicate = createDataEntryPredicates(toCriteria(query.filters))
    val filtered = dataEntries.values
      .filter { query.applyFilter(it) }
      .filter { filterByPredicate(it, predicate) }
      .toList()

    val comparator = dataComparator(query.sort)

    val sorted = if (comparator != null) {
      filtered.sortedWith(comparator)
    } else {
      filtered
    }

    return QueryResponseMessageResponseType.asQueryResponseMessage(
      payload = DataEntriesQueryResult(elements = sorted).slice(query = query),
      metaData = revisionSupport.getRevisionMax(sorted.map { it.identity }).toMetaData()
    )
  }


  /**
   * Updates query for provided data entry identity.
   * @param identity id of the data entry.
   */
  private fun updateDataEntryQuery(identity: String) {
    val revisionValue = revisionSupport.getRevisionMax(setOf(identity))
    logger.debug { "SIMPLE-VIEW-33: Updating query with new element $identity with revision $revisionValue" }

    val entry = dataEntries.getValue(identity)
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
