package io.holunda.camunda.taskpool.api.task

import org.axonframework.modelling.command.TargetAggregateIdentifier
import org.camunda.bpm.engine.variable.VariableMap
import java.util.*

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
 * Un-assign a user from the task.
 */
data class UnclaimInteractionTaskCommand(
  @TargetAggregateIdentifier
  override val id: String,
  override val sourceReference: SourceReference,
  override val taskDefinitionKey: String
) : InteractionTaskCommand

/**
 * Complete a task using provided payload and an optional user (as assignee).
 */
data class CompleteInteractionTaskCommand(
  @TargetAggregateIdentifier
  override val id: String,
  override val sourceReference: SourceReference,
  override val taskDefinitionKey: String,
  val payload: VariableMap,
  val assignee: String?
): InteractionTaskCommand

/**
 * Set follow-up date for the task.
 */
data class DeferInteractionTaskCommand(
  @TargetAggregateIdentifier
  override val id: String,
  override val sourceReference: SourceReference,
  override val taskDefinitionKey: String,
  val followUpDate: Date
): InteractionTaskCommand

/**
 * Set follow-up date for the task.
 */
data class UndeferInteractionTaskCommand(
  @TargetAggregateIdentifier
  override val id: String,
  override val sourceReference: SourceReference,
  override val taskDefinitionKey: String
): InteractionTaskCommand
