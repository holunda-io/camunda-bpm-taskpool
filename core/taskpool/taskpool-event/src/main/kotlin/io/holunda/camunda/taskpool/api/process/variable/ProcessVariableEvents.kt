package io.holunda.camunda.taskpool.api.process.variable

import io.holunda.camunda.taskpool.api.task.SourceReference
import org.axonframework.serialization.Revision

/**
 * Variable has been created.
 */
@Revision("1")
data class ProcessVariableCreatedEvent(
  val sourceReference: SourceReference,
  val variableName: String,
  val variableInstanceId: String,
  val scopeActivityInstanceId: String,
  val value: ProcessVariableValue
)

/**
 * Variable has been updated.
 */
@Revision("1")
data class ProcessVariableUpdatedEvent(
  val sourceReference: SourceReference,
  val variableName: String,
  val variableInstanceId: String,
  val scopeActivityInstanceId: String,
  val value: ProcessVariableValue
)

/**
 * Variable has been deleted.
 */
@Revision("1")
data class ProcessVariableDeletedEvent(
  val sourceReference: SourceReference,
  val variableName: String,
  val variableInstanceId: String,
  val scopeActivityInstanceId: String,
)

