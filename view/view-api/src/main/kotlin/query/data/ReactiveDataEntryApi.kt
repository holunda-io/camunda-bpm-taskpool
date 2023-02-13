package io.holunda.polyflow.view.query.data

import io.holunda.polyflow.view.DataEntry
import org.axonframework.messaging.MetaData
import java.util.concurrent.CompletableFuture

/**
 * Defines a reactive API interface for Data Entry Queries.
 */
interface ReactiveDataEntryApi {

  /**
   * Query data entries for id.
   * @param query object representing data identity.
   * @param metaData metadata for the query, may be empty.
   * @return completable future with data entries.
   */
  fun query(query: DataEntryForIdentityQuery, metaData: MetaData = MetaData.emptyInstance()): CompletableFuture<DataEntry>

  /**
   * Query data entries for type.
   * @param query object representing data identity.
   * @param metaData metadata for the query, may be empty.
   * @return completable future with data entries.
   */
  fun query(query: DataEntriesForDataEntryTypeQuery, metaData: MetaData = MetaData.emptyInstance()): CompletableFuture<DataEntriesQueryResult>

  /**
   * Query data entries for user.
   * @param query object representing the user query.
   * @param metaData metadata for the query, may be empty.
   * @return completable future with data entries.
   */
  fun query(query: DataEntriesForUserQuery, metaData: MetaData = MetaData.emptyInstance()): CompletableFuture<DataEntriesQueryResult>

  /**
   * Query data entries.
   * @param query object
   * @param metaData meta of the query, may be empty.
   * @return query result as future.
   */
  fun query(query: DataEntriesQuery, metaData: MetaData = MetaData.emptyInstance()): CompletableFuture<DataEntriesQueryResult>

}
