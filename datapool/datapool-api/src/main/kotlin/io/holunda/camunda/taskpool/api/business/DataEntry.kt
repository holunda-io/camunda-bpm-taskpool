package io.holunda.camunda.taskpool.api.business

import io.holunda.camunda.taskpool.api.business.Modification.Companion.NONE
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables
import java.time.OffsetDateTime

/**
 * Represents a data entry.
 */
data class DataEntry(

  /**
   * Entry type
   */
  val entryType: EntryType,
  /**
   * Entry id.
   */
  val entryId: EntryId,
  /**
   * Human readable type e.g. "purchase order"
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
  val modification: Modification = NONE,
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


interface DataEntryState {
  val processingType: ProcessingType
  val state: String?
}

data class DataEntryStateImpl(
  override val processingType: ProcessingType = ProcessingType.UNDEFINED,
  override val state: String = ""
) : DataEntryState

enum class ProcessingType {
  PRELIMINARY,
  IN_PROGRESS,
  COMPLETED,
  CANCELLED,
  UNDEFINED;

  fun asState(state: String = "") = DataEntryStateImpl(processingType = this, state = state)
}


data class Modification(
  /**
   * Time asState update
   */
  val time: OffsetDateTime? = null,
  /**
   * Username asState the user who updated the business entry.
   */
  val username: String? = null,
  /**
   * Log entry for the update.
   */
  val log: String? = null,

  /**
   * Log entry details.
   */
  val logNotes: String? = null
) {
  companion object {
    val NONE = Modification()

    fun now() = Modification(time = OffsetDateTime.now())
  }
}
