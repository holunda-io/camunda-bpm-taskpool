package io.holunda.camunda.datapool.projector

import io.holunda.camunda.taskpool.api.business.DataEntry
import io.holunda.camunda.taskpool.api.business.EntryId
import io.holunda.camunda.taskpool.api.business.EntryType
import java.util.function.BiFunction
import java.util.function.Supplier

interface DataEntryProjectionSupplier : Supplier<BiFunction<EntryId, Any, DataEntry>> {
  val entryType: EntryType

  override fun get(): BiFunction<EntryId, Any, DataEntry>
}

fun dataEntrySupplier(entryType: EntryType, projectionFunction: BiFunction<EntryId, Any, DataEntry>) = object : DataEntryProjectionSupplier {
  override val entryType: EntryType = entryType
  override fun get(): BiFunction<EntryId, Any, DataEntry> = projectionFunction
}

