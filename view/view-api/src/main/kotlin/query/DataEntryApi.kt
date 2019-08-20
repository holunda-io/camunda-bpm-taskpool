package io.holunda.camunda.taskpool.view.query

import io.holunda.camunda.taskpool.view.query.data.DataEntriesForUserQuery
import io.holunda.camunda.taskpool.view.query.data.DataEntryForIdentityQuery
import io.holunda.camunda.taskpool.view.query.data.DataEntriesQueryResult

/**
 * Defines an API interface for Data Entry Queries.
 */
interface DataEntryApi {

  fun query(query: DataEntryForIdentityQuery): DataEntriesQueryResult

  fun query(query: DataEntriesForUserQuery): DataEntriesQueryResult
}
