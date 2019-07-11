package io.holunda.camunda.taskpool.view.simple.service

import io.holunda.camunda.taskpool.api.business.*
import io.holunda.camunda.taskpool.api.business.AuthorizationChange.Companion.applyGroupAuthorization
import io.holunda.camunda.taskpool.api.business.AuthorizationChange.Companion.applyUserAuthorization
import io.holunda.camunda.taskpool.view.DataEntry
import io.holunda.camunda.taskpool.view.query.DataEntryApi
import io.holunda.camunda.taskpool.view.query.data.DataEntriesForUserQuery
import io.holunda.camunda.taskpool.view.query.data.DataEntriesQueryResult
import io.holunda.camunda.taskpool.view.query.data.DataEntryForIdentityQuery
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
    val entryId = dataIdentityString(entryType = event.entryType, entryId = event.entryId)
    dataEntries[entryId] = event.toDataEntry()

    updateDataEntryQuery(entryId)
  }

  @Suppress("unused")
  @EventHandler
  fun on(event: DataEntryUpdatedEvent) {
    logger.debug { "Business data entry updated $event" }
    val entryId = dataIdentityString(entryType = event.entryType, entryId = event.entryId)
    dataEntries[entryId] = event.toDataEntry(dataEntries[entryId])
    updateDataEntryQuery(entryId)
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


fun DataEntryUpdatedEvent.toDataEntry(oldEntry: DataEntry?) = if (oldEntry == null) {
  DataEntry(
    entryType = this.entryType,
    entryId = this.entryId,
    payload = this.payload,
    correlations = this.correlations,
    name = this.name,
    applicationName = this.applicationName,
    type = this.type,
    description = this.description,
    state = this.state,
    formKey = this.formKey,
    authorizedUsers = applyUserAuthorization(listOf(), this.authorizations),
    authorizedGroups = applyGroupAuthorization(listOf(), this.authorizations)
  )
} else {
  oldEntry.copy(
    payload = this.payload,
    correlations = this.correlations,
    name = this.name,
    applicationName = this.applicationName,
    type = this.type,
    description = this.description,
    state = this.state,
    formKey = this.formKey,
    authorizedUsers = applyUserAuthorization(oldEntry.authorizedUsers, this.authorizations),
    authorizedGroups = applyGroupAuthorization(oldEntry.authorizedGroups, this.authorizations)
  )
}

fun DataEntryCreatedEvent.toDataEntry() = DataEntry(
  entryType = this.entryType,
  entryId = this.entryId,
  payload = this.payload,
  correlations = this.correlations,
  name = this.name,
  applicationName = this.applicationName,
  type = this.type,
  description = this.description,
  state = this.state,
  formKey = this.formKey,
  authorizedUsers = applyUserAuthorization(listOf(), this.authorizations),
  authorizedGroups = applyGroupAuthorization(listOf(), this.authorizations)
)


