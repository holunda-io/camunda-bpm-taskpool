package io.holunda.camunda.taskpool.api.business

import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables

/**
 * Represents a data entry change.
 */
data class DataEntryChange(
  /**
   * Entry type
   */
  val entryType: EntryType,
  /**
   * Entry id.
   */
  val entryId: EntryId,
  /**
   * Human-readable type e.g. "purchase order"
   */
  val type: String,
  /**
   * Application name (origin)
   */
  val applicationName: String,
  /**
   * Human-readable identifier or name, e.g. "BANF-4711 - TV for meeting room"
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
   * Modification information.
   */
  val modification: Modification = Modification.NONE,
  /**
   * Authorization information.
   */
  val authorizationChanges: List<AuthorizationChange> = listOf(),
  /**
   * Form key.
   */
  val formKey: String? = null
)
