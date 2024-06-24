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
  val createModification: Modification = Modification.now(),
  /**
   * List of authorized users.
   */
  val authorizations: List<AuthorizationChange> = listOf(),
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
  val updateModification: Modification = Modification.now(),
  /**
   * List of authorizations.
   */
  val authorizations: List<AuthorizationChange> = listOf(),
  /**
   * Form key.
   */
  val formKey: String? = null
)


/**
 * Data entry deleted.
 */
@Revision("1")
data class DataEntryDeletedEvent(
  /**
   * Entry type
   */
  val entryType: EntryType,
  /**
   * Entry id.
   */
  val entryId: EntryId,
  /**
   * Modification information.
   */
  val deleteModification: Modification = Modification.now(),
  /**
   * State of data entry.
   */
  val state: DataEntryState = ProcessingType.UNDEFINED.of("")
)

/**
 * Data entry anonymized.
 */
@Revision("1")
data class DataEntryAnonymizedEvent(
  /**
   * Entry type
   */
  val entryType: EntryType,

  /**
   * Entry id.
   */
  val entryId: EntryId,

  /**
   * The username that will replace the current username(s) in the protocol of the data entry
   */
  val anonymizedUsername: String,

  /**
   * Usernames that should be excluded from the anonymization. For example "SYSTEM"
   */
  val excludedUsernames: List<String> = listOf(),

  /**
   * Modification information.
   */
  val anonymizeModification: Modification = Modification.now(),

)