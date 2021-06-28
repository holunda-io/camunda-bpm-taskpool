package io.holunda.polyflow.view.query.data

import org.axonframework.messaging.MetaData
import org.axonframework.queryhandling.QueryResponseMessage

/**
 * Defines an API interface for Data Entry Queries.
 */
interface DataEntryApi {

  /**
   * Query data entries for given id.
   * @param query object
   * @param metaData meta of the query, may be empty.
   * @return query result.
   */
  fun query(query: DataEntryForIdentityQuery, metaData: MetaData = MetaData.emptyInstance()): QueryResponseMessage<DataEntriesQueryResult>

  /**
   * Query data entries for provided user.
   * @param metaData meta of the query, may be empty.
   * @param query object
   * @return query result.
   */
  fun query(query: DataEntriesForUserQuery, metaData: MetaData = MetaData.emptyInstance()): QueryResponseMessage<DataEntriesQueryResult>

  /**
   * Query data entries.
   * @param query object
   * @param metaData meta of the query, may be empty.
   * @return query result.
   */
  fun query(query: DataEntriesQuery, metaData: MetaData = MetaData.emptyInstance()): QueryResponseMessage<DataEntriesQueryResult>

}
