package io.holunda.camunda.taskpool.api.process.variable

import io.holunda.camunda.taskpool.api.task.SourceReference
import org.axonframework.serialization.Revision

/**
 * Variable has been changed.
 */
@Revision("1")
data class ProcessVariablesChangedEvent(
  val sourceReference: SourceReference,
  val variableChanges: List<ProcessVariableChange>
)
