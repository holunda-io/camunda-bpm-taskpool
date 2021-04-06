package io.holunda.camunda.taskpool.view

import io.holunda.camunda.taskpool.api.business.*
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables
import java.util.*

/**
 * Data entry projection.
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
   * Type e.g. "purchase order"
   */
  val type: String,
  /**
   * Application name (origin)
   */
  val applicationName: String,
  /**
   * Human readable identifier or name, e.g. "BANF-4711 - TV for meeting room"
   */
  val name: String,
  /**
   * Correlations.
   */
  val correlations: CorrelationMap = newCorrelations(),
  /**
   * Payload.
   */
  val payload: VariableMap = Variables.createVariables(),
  /**
   * Human readable description, e.g. "TV in meeting room Hamburg is broken and a new one should be installed."
   */
  val description: String? = null,
  /**
   * Current state of data entry.
   */
  val state: DataEntryState = ProcessingType.UNDEFINED.of(""),
  /**
   * List of authorized users.
   */
  val authorizedUsers: List<String> = listOf(),
  /**
   * List of authorized groups.
   */
  val authorizedGroups: List<String> = listOf(),
  /**
   * Form key.
   */
  val formKey: String? = null,

  /**
   * Protocol of changes.
   */
  val protocol: List<ProtocolEntry> = listOf()

) : DataIdentity {
  val identity by lazy {
    dataIdentityString(entryType, entryId)
  }
}
