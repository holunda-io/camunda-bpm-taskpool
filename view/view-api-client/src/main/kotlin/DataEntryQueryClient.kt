package io.holunda.polyflow.view

import io.holixon.axon.gateway.query.QueryResponseMessageResponseType
import io.holixon.axon.gateway.query.RevisionQueryParameters
import io.holunda.polyflow.view.query.data.DataEntriesForUserQuery
import io.holunda.polyflow.view.query.data.DataEntriesQuery
import io.holunda.polyflow.view.query.data.DataEntriesQueryResult
import io.holunda.polyflow.view.query.data.DataEntryForIdentityQuery
import org.axonframework.messaging.GenericMessage
import org.axonframework.messaging.MetaData
import org.axonframework.queryhandling.QueryGateway
import java.util.concurrent.CompletableFuture

/**
 * Client encapsulating the correct query types (including response types)
 */
class DataEntryQueryClient(
  private val queryGateway: QueryGateway
) {

  /**
   * @see io.holunda.polyflow.view.query.data.DataEntryApi.query
   * @see io.holunda.polyflow.view.query.data.DataEntriesForUserQuery
   */
  fun query(query: DataEntriesForUserQuery, revisionParams: RevisionQueryParameters? = null): CompletableFuture<DataEntriesQueryResult> =
    queryGateway.query(
      GenericMessage.asMessage(query).andMetaData(revisionParams?.toMetaData() ?: MetaData.emptyInstance()),
      QueryResponseMessageResponseType.queryResponseMessageResponseType<DataEntriesQueryResult>()
    )

  /**
   * @see io.holunda.polyflow.view.query.data.DataEntryApi.query
   * @see io.holunda.polyflow.view.query.data.DataEntryForIdentityQuery
   */
  fun query(query: DataEntryForIdentityQuery, revisionParams: RevisionQueryParameters? = null): CompletableFuture<DataEntriesQueryResult> =
    queryGateway.query(
      GenericMessage.asMessage(query).andMetaData(revisionParams?.toMetaData() ?: MetaData.emptyInstance()),
      QueryResponseMessageResponseType.queryResponseMessageResponseType<DataEntriesQueryResult>()
    )

  /**
   * @see io.holunda.polyflow.view.query.data.DataEntryApi.query
   * @see io.holunda.polyflow.view.query.data.DataEntriesQuery
   */
  fun query(query: DataEntriesQuery, revisionParams: RevisionQueryParameters? = null): CompletableFuture<DataEntriesQueryResult> =
    queryGateway.query(
      GenericMessage.asMessage(query).andMetaData(revisionParams?.toMetaData() ?: MetaData.emptyInstance()),
      QueryResponseMessageResponseType.queryResponseMessageResponseType<DataEntriesQueryResult>()
    )
}
