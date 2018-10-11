package io.holunda.camunda.taskpool.view.simple.query

import io.holunda.camunda.taskpool.api.business.EntryId
import io.holunda.camunda.taskpool.api.business.EntryType
import io.holunda.camunda.taskpool.view.simple.service.DataEntry

data class DataEntryQuery(
  val entryType: EntryType,
  val entryId: EntryId? = null
) : FilterQuery<DataEntry> {
  override fun applyFilter(dataEntry: DataEntry) =
// entry type
    dataEntry.entryType == this.entryType
// if id is specified, applyFilter by it
      && (this.entryId == null || dataEntry.entryId == this.entryId)

}


