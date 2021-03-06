package io.holunda.camunda.taskpool.api.process.instance

import io.holunda.camunda.taskpool.api.task.SourceReference
import org.axonframework.modelling.command.TargetAggregateIdentifier

/**
 * Command related to process instance.
 */
interface ProcessInstanceCommand {
  val sourceReference: SourceReference
  val processInstanceId: String
}

/**
 * Informs about a process instance started in the engine.
 */
data class StartProcessInstanceCommand(

  @TargetAggregateIdentifier
  override val processInstanceId: String,
  override val sourceReference: SourceReference,

  /** the business key of the process instance  */
  val businessKey: String? = null,

  /** the id of the user that started the process instance  */
  val startUserId: String? = null,

  /** the id of the super case instance  */
  val superInstanceId: String? = null,

  /** id of the activity which started the process instance  */
  val startActivityId: String? = null

) : ProcessInstanceCommand

/**
 * Informs about a process instance ended in the engine.
 */
abstract class EndProcessInstanceCommand : ProcessInstanceCommand

/**
 * Informs about a process instance finished in the engine.
 */
data class FinishProcessInstanceCommand(
  @TargetAggregateIdentifier
  override val processInstanceId: String,
  override val sourceReference: SourceReference,

  /** the business key of the process instance  */
  val businessKey: String? = null,

  /** the id of the super case instance  */
  val superInstanceId: String? = null,

  /** id of the activity which ended the process instance */
  val endActivityId: String?,
): EndProcessInstanceCommand()

/**
 * Informs about a process instance cancelled by the user in the engine.
 */
data class CancelProcessInstanceCommand(
  @TargetAggregateIdentifier
  override val processInstanceId: String,
  override val sourceReference: SourceReference,

  /** the business key of the process instance  */
  val businessKey: String? = null,

  /** the id of the super case instance  */
  val superInstanceId: String? = null,

  /** id of the activity which ended the process instance */
  val endActivityId: String?,

  /** the reason why this process instance was cancelled (deleted) */
  val deleteReason: String?
): EndProcessInstanceCommand()

/**
 * Informs about a process instance updated in the engine.
 */
abstract class UpdateProcessInstanceCommand : ProcessInstanceCommand

/**
 * Informs about a process instance suspended in the engine.
 */
data class SuspendProcessInstanceCommand(
  @TargetAggregateIdentifier
  override val processInstanceId: String,
  override val sourceReference: SourceReference,
) : UpdateProcessInstanceCommand()

/**
 * Informs about a process instance resumed in the engine.
 */
data class ResumeProcessInstanceCommand(
  @TargetAggregateIdentifier
  override val processInstanceId: String,
  override val sourceReference: SourceReference,
) : UpdateProcessInstanceCommand()
