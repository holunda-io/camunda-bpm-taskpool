package io.holunda.polyflow.example.process.approval.service

import io.holixon.axon.gateway.query.QueryResponseMessageResponseType
import io.holixon.axon.gateway.query.RevisionQueryParameters
import io.holixon.axon.gateway.query.RevisionValue
import io.holunda.polyflow.datapool.sender.DataEntryCommandSender
import io.holunda.camunda.taskpool.api.business.*
import io.holunda.polyflow.view.DataEntry
import io.holunda.polyflow.view.auth.User
import io.holunda.polyflow.view.query.data.DataEntriesForUserQuery
import io.holunda.polyflow.view.query.data.DataEntriesQuery
import io.holunda.polyflow.view.query.data.DataEntriesQueryResult
import org.axonframework.messaging.GenericMessage
import org.axonframework.messaging.MetaData
import org.axonframework.queryhandling.QueryGateway
import org.springframework.stereotype.Component

/**
 * Repository for storing and retrievieng data entries using data entry
 * projection of data pool.
 */
@Component
class RequestDataEntryRepository(
  private val queryGateway: QueryGateway,
  private val sender: DataEntryCommandSender
) {

  /**
   * Stores a data entry.
   */
  fun save(dataEntryChange: DataEntryChange, revision: RevisionValue) {
    sender.sendDataEntryChange(command = CreateOrUpdateDataEntryCommand(dataEntryChange), metaData = revision.toMetaData())
  }

  /**
   * Save the entry.
   */
  fun save(
    entryType: String,
    entryId: String,
    payload: Any,
    state: DataEntryState,
    name: String,
    description: String,
    type: String,
    modification: Modification,
    authorizationChanges: List<AuthorizationChange>,
    metaData: MetaData) {
    sender.sendDataEntryChange(entryType = entryType, entryId = entryId, payload = payload, state = state, name = name, description = description, type = type, modification = modification, authorizationChanges = authorizationChanges, metaData = metaData)
  }

  /**
   * Get all data entries visible for one user.
   */
  fun getAllForUser(user: User, revisionQuery: RevisionQueryParameters): List<DataEntry> = queryGateway.query(
    GenericMessage.asMessage(DataEntriesForUserQuery(user = user, page = 1, size = Int.MAX_VALUE, sort = "", filters = listOf())).andMetaData(revisionQuery.toMetaData()),
    QueryResponseMessageResponseType.queryResponseMessageResponseType<DataEntriesQueryResult>()
  ).join().elements


  /**
   * Get all data entries.
   */
  fun getAll(revisionQuery: RevisionQueryParameters): List<DataEntry> = queryGateway.query(
    GenericMessage.asMessage(DataEntriesQuery(page = 1, size = Int.MAX_VALUE, sort = "", filters = listOf())).andMetaData(revisionQuery.toMetaData()),
    QueryResponseMessageResponseType.queryResponseMessageResponseType<DataEntriesQueryResult>()
  ).join().elements

}

