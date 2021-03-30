package io.holunda.camunda.taskpool.api.process.variable

import io.holunda.camunda.taskpool.api.task.SourceReference
import org.axonframework.serialization.Revision

@Revision("1")
data class ProcessVariableCreatedEvent(
  val sourceReference: SourceReference,
)

@Revision("1")
data class ProcessVariableUpdatedEvent(
  val sourceReference: SourceReference,
)
