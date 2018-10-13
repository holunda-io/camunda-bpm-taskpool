package io.holunda.camunda.taskpool.view

import io.holunda.camunda.taskpool.api.business.DataIdentity
import io.holunda.camunda.taskpool.api.business.EntryId
import io.holunda.camunda.taskpool.api.business.EntryType

data class DataEntry(
  override val entryType: EntryType,
  override val entryId: EntryId,
  val payload: Any
) : DataIdentity
