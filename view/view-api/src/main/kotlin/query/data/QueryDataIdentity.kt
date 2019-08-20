package io.holunda.camunda.taskpool.view.query.data

import io.holunda.camunda.taskpool.api.business.DataIdentity
import io.holunda.camunda.taskpool.api.business.EntryId
import io.holunda.camunda.taskpool.api.business.EntryType

/**
 * Identity used in queries.
 */
data class QueryDataIdentity(
  override val entryType: EntryType,
  override val entryId: EntryId
) : DataIdentity
