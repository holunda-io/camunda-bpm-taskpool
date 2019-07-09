package io.holunda.camunda.taskpool.view.simple.service

import io.holunda.camunda.taskpool.api.business.DataEntryCreatedEvent
import io.holunda.camunda.taskpool.api.business.DataEntryUpdatedEvent
import io.holunda.camunda.taskpool.api.business.dataIdentity
import io.holunda.camunda.taskpool.view.DataEntry
import io.holunda.camunda.taskpool.view.query.DataEntryApi
import io.holunda.camunda.taskpool.view.query.data.DataEntriesForUserQuery
import io.holunda.camunda.taskpool.view.query.data.DataEntryForIdentityQuery
import io.holunda.camunda.taskpool.view.query.data.DataEntriesQueryResult
import io.holunda.camunda.taskpool.view.toDataEntry
import mu.KLogging
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.axonframework.queryhandling.QueryHandler
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
@ProcessingGroup(SimpleServiceViewProcessingGroup.PROCESSING_GROUP)
class DataEntryService(
  private val queryUpdateEmitter: QueryUpdateEmitter
) : DataEntryApi {

  companion object : KLogging()

  private val dataEntries = ConcurrentHashMap<String, DataEntry>()

  @Suppress("unused")
  @EventHandler
  fun on(event: DataEntryCreatedEvent) {
    logger.debug { "Business data entry created $event" }
    dataEntries[dataIdentity(entryType = event.entryType, entryId = event.entryId)] = event.toDataEntry()
    updateDataEntryQuery(dataIdentity(entryType = event.entryType, entryId = event.entryId))
  }

  @Suppress("unused")
  @EventHandler
  fun on(event: DataEntryUpdatedEvent) {
    logger.debug { "Business data entry updated $event" }
    dataEntries[dataIdentity(entryType = event.entryType, entryId = event.entryId)] = event.toDataEntry()
    updateDataEntryQuery(dataIdentity(entryType = event.entryType, entryId = event.entryId))
  }

  /**
   * Retrieves a list of all data entries of given entry type (and optional id).
   */
  @QueryHandler
  override fun query(query: DataEntryForIdentityQuery) = DataEntriesQueryResult(dataEntries.values.filter { query.applyFilter(it) })

  /**
   * Retrieves a list of all data entries visible for current user.
   */
  @QueryHandler
  override fun query(query: DataEntriesForUserQuery) = DataEntriesQueryResult(dataEntries.values.filter { query.applyFilter(it) }).slice(query)


  private fun updateDataEntryQuery(identity: String) = queryUpdateEmitter.updateMapFilterQuery(dataEntries, identity, DataEntriesForUserQuery::class.java)

}
