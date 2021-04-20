package io.holunda.camunda.datapool.projector

import io.holunda.camunda.taskpool.api.business.EntryType
import org.springframework.stereotype.Component

/**
 * Component responsible for retrieving property projectors for given entry types.
 */
@Component
class DataEntryProjector(private val suppliers: List<DataEntryProjectionSupplier>) {
  /**
   * Retrieve a list of projection suppliers for a given entry type.
   * @return a projection supplier if registered for this entry type, or null.
   */
  fun getProjection(entryType: EntryType): DataEntryProjectionSupplier? = suppliers.find { it.entryType == entryType }
}
