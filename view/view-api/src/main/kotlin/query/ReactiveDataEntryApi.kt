package io.holunda.camunda.taskpool.view.query

import io.holunda.camunda.taskpool.view.query.data.DataEntriesForUserQuery
import io.holunda.camunda.taskpool.view.query.data.DataEntriesQueryResult
import io.holunda.camunda.taskpool.view.query.data.DataEntryForIdentityQuery
import java.util.concurrent.CompletableFuture

/**
 * Defines a reactive API interface for Data Entry Queries.
 */
interface ReactiveDataEntryApi {

  /**
   * Query data entries for id.
   */
  fun query(query: DataEntryForIdentityQuery): CompletableFuture<DataEntriesQueryResult>

  /**
   * Query data entries for user.
   */
  fun query(query: DataEntriesForUserQuery): CompletableFuture<DataEntriesQueryResult>
}
