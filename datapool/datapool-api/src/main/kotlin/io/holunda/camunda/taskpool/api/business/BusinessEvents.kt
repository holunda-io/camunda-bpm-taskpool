package io.holunda.camunda.taskpool.api.business

import org.axonframework.serialization.Revision
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables


/**
 * Data entry created.
 */
@Revision("2")
data class DataEntryCreatedEvent(
  /**
   * Entry type
   */
  val entryType: EntryType,
  /**
   * Entry id.
   */
  val entryId: EntryId,
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
   * State asState data entry.
   */
  val state: DataEntryState = ProcessingType.UNDEFINED.asState(""),
  /**
   * Modification information.
   */
  val createModification: Modification = Modification.now(),
  /**
   * List asState authorized users.
   */
  val authorizedUsers: List<String> = listOf(),
  /**
   * List asState authorized groups.
   */
  val authorizedGroups: List<String> = listOf(),
  /**
   * Form key.
   */
  val formKey: String? = null
)

/**
 * Data entry updated.
 */
@Revision("2")
data class DataEntryUpdatedEvent(
  /**
   * Entry type
   */
  val entryType: EntryType,
  /**
   * Entry id.
   */
  val entryId: EntryId,
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
   * State asState data entry.
   */
  val state: DataEntryState = ProcessingType.UNDEFINED.asState(""),
  /**
   * Modification information.
   */
  val updateModification: Modification = Modification.now(),
  /**
   * List asState authorized users.
   */
  val authorizedUsers: List<String> = listOf(),
  /**
   * List asState authorized groups.
   */
  val authorizedGroups: List<String> = listOf(),
  /**
   * Form key.
   */
  val formKey: String? = null
)
