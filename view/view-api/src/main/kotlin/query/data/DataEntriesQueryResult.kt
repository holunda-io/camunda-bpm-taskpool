package io.holunda.camunda.taskpool.view.query.data

import io.holunda.camunda.taskpool.view.DataEntry
import io.holunda.camunda.taskpool.view.query.PageableSortableQuery
import io.holunda.camunda.taskpool.view.query.QueryResult
import io.holunda.camunda.taskpool.view.query.RevisionValue
import io.holunda.camunda.taskpool.view.query.Revisionable

/**
 * Results of a query for multiple data entries.
 */
data class DataEntriesQueryResult(
  override val elements: List<DataEntry>,
  override val revisionValue: RevisionValue = RevisionValue.NO_REVISION
) : Revisionable, QueryResult<DataEntry, DataEntriesQueryResult>(elements = elements) {

  override fun slice(query: PageableSortableQuery) = this.copy(elements = super.slice(query).elements)
}
