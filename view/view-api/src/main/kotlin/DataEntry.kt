package io.holunda.camunda.taskpool.view

import io.holunda.camunda.taskpool.api.business.*
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables

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
   * State of data entry.
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
  val formKey: String? = null


) : DataIdentity {
  val identity by lazy {
    dataIdentity(entryType, entryId)
  }
}


fun DataEntryUpdatedEvent.toDataEntry() = DataEntry(
  entryType = this.entryType,
  entryId = this.entryId,
  payload = this.payload,
  correlations = this.correlations,
  name = this.name,
  applicationName = this.applicationName,
  type = this.type,
  description = this.description,
  authorizedUsers = this.authorizedUsers,
  authorizedGroups = this.authorizedGroups,
  state = this.state,
  formKey = this.formKey
)

fun DataEntryCreatedEvent.toDataEntry() = DataEntry(
  entryType = this.entryType,
  entryId = this.entryId,
  payload = this.payload,
  correlations = this.correlations,
  name = this.name,
  applicationName = this.applicationName,
  type = this.type,
  description = this.description,
  authorizedUsers = this.authorizedUsers,
  authorizedGroups = this.authorizedGroups,
  state = this.state,
  formKey = this.formKey
)
