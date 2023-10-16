package io.holunda.polyflow.view.jpa.data

import io.holunda.camunda.taskpool.api.business.DataEntryState
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.io.Serializable

/**
 * Represents stat of data entry.
 */
@Embeddable
class DataEntryStateEmbeddable(
  @Column(name = "PROCESSING_TYPE", length = 64, nullable = false)
  var processingType: String,
  @Column(name = "STATE", length = 64, nullable = false)
  var state: String
) : Serializable {
  companion object {
    /**
     * Factory method.
     */
    operator fun invoke(state: DataEntryState): DataEntryStateEmbeddable =
      DataEntryStateEmbeddable(processingType = state.processingType.name, state = state.state ?: "")
  }
}
