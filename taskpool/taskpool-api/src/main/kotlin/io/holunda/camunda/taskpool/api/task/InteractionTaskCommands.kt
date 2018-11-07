package io.holunda.camunda.taskpool.api.task

import org.axonframework.modelling.command.TargetAggregateIdentifier
import org.camunda.bpm.engine.variable.VariableMap

/**
 * Assign a user to the task.
 */
data class ClaimInteractionTaskCommand(
  @TargetAggregateIdentifier
  override val id: String,
  override val sourceReference: SourceReference,
  override val taskDefinitionKey: String,
  val assignee: String
) : InteractionTaskCommand

/**
 * Unassign a user from the task.
 */
data class UnclaimInteractionTaskCommand(
  @TargetAggregateIdentifier
  override val id: String,
  override val sourceReference: SourceReference,
  override val taskDefinitionKey: String
) : InteractionTaskCommand

/**
 * Complete a task usgin provided payload and an optional user (as assignee).
 */
data class CompleteInteractionTaskCommand(
  @TargetAggregateIdentifier
  override val id: String,
  override val sourceReference: SourceReference,
  override val taskDefinitionKey: String,
  val payload: VariableMap,
  val assignee: String?
): InteractionTaskCommand
