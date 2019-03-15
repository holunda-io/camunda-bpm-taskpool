package io.holunda.camunda.taskpool.view

import io.holunda.camunda.taskpool.api.business.DataIdentity
import io.holunda.camunda.taskpool.api.business.EntryId
import io.holunda.camunda.taskpool.api.business.EntryType
import io.holunda.camunda.taskpool.api.business.dataIdentity
import org.camunda.bpm.engine.variable.VariableMap

/**
 * Data entry.
 */
data class DataEntry(
  /**
   * Type of entry.
   */
  override val entryType: EntryType,
  /**
   * Id of entry of this type.
   */
  override val entryId: EntryId,
  /**
   * Payload.
   */
  val payload: VariableMap
) : DataIdentity {
  val identity by lazy {
    dataIdentity(entryType, entryId)
  }
}
