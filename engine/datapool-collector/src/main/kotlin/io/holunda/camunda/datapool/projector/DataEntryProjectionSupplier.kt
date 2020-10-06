package io.holunda.camunda.datapool.projector

import io.holunda.camunda.taskpool.api.business.DataEntryChange
import io.holunda.camunda.taskpool.api.business.EntryId
import io.holunda.camunda.taskpool.api.business.EntryType
import java.util.function.BiFunction
import java.util.function.Supplier
// FIXME: Document me, here and in the docs.
interface DataEntryProjectionSupplier : Supplier<BiFunction<EntryId, Any, DataEntryChange>> {
  val entryType: EntryType

  override fun get(): BiFunction<EntryId, Any, DataEntryChange>
}

fun dataEntrySupplier(entryType: EntryType, projectionFunction: BiFunction<EntryId, Any, DataEntryChange>) = object : DataEntryProjectionSupplier {
  override val entryType: EntryType = entryType
  override fun get(): BiFunction<EntryId, Any, DataEntryChange> = projectionFunction
}

