package io.holunda.polyflow.datapool.projector

import io.holunda.camunda.taskpool.api.business.DataEntryChange
import io.holunda.camunda.taskpool.api.business.EntryId
import io.holunda.camunda.taskpool.api.business.EntryType
import java.util.function.BiFunction
import java.util.function.Supplier

/**
 * Supplier for given entry type. Used to convert a business object of any type to data entry change reflecting it.
 */
interface DataEntryProjectionSupplier : Supplier<BiFunction<EntryId, Any, DataEntryChange>> {
  val entryType: EntryType
  override fun get(): BiFunction<EntryId, Any, DataEntryChange>
}

/**
 * Data entry projection supplier implemented by a provided projection function.
 * @param entryType type of business entries.
 * @param projectionFunction a (bi-)function which is used to create a data entry change projection from a business entity.
 * @return supplier.
 */
fun dataEntrySupplier(entryType: EntryType, projectionFunction: BiFunction<EntryId, Any, DataEntryChange>) = object : DataEntryProjectionSupplier {
  override val entryType: EntryType = entryType
  override fun get(): BiFunction<EntryId, Any, DataEntryChange> = projectionFunction
}

