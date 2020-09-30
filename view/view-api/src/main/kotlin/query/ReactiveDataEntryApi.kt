package io.holunda.camunda.taskpool.view.query

import io.holunda.camunda.taskpool.view.query.data.DataEntriesForUserQuery
import io.holunda.camunda.taskpool.view.query.data.DataEntriesQuery
import io.holunda.camunda.taskpool.view.query.data.DataEntriesQueryResult
import io.holunda.camunda.taskpool.view.query.data.DataEntryForIdentityQuery
import org.axonframework.messaging.MetaData
import java.util.concurrent.CompletableFuture

/**
 * Defines a reactive API interface for Data Entry Queries.
 */
interface ReactiveDataEntryApi {

  /**
   * Query data entries for id.
   * @param query object representing data identity.
   * @param metaData metadata for the query, may be null.
   * @return completable future with data entries.
   */
  fun query(query: DataEntryForIdentityQuery, metaData: MetaData? = null): CompletableFuture<DataEntriesQueryResult>

  /**
   * Query data entries for user.
   * @param query object representing the user query.
   * @param metaData metadata for the query, may be null.
   * @return completable future with data entries.
   */
  fun query(query: DataEntriesForUserQuery, metaData: MetaData? = null): CompletableFuture<DataEntriesQueryResult>

  /**
   * Query data entries.
   * @param query object
   * @param metaData meta of the query, may be null.
   * @return query result as future.
   */
  fun query(query: DataEntriesQuery, metaData: MetaData? = null): CompletableFuture<DataEntriesQueryResult>

}
