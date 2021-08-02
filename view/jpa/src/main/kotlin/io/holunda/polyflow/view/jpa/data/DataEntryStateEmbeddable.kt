package io.holunda.polyflow.view.jpa.data

import io.holunda.camunda.taskpool.api.business.DataEntryState
import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
class DataEntryStateEmbeddable(
  @Column(name = "PROCESSING_TYPE", nullable = false)
  val processingType: String,
  @Column(name = "STATE", nullable = false)
  val state: String
) : Serializable {
  companion object {
    operator fun invoke(state: DataEntryState): DataEntryStateEmbeddable =
      DataEntryStateEmbeddable(processingType = state.processingType.name, state = state.state ?: "")
  }
}
