package io.holunda.camunda.taskpool.api.process.instance

import io.holunda.camunda.taskpool.api.business.CorrelationMap
import io.holunda.camunda.taskpool.api.business.WithCorrelations
import io.holunda.camunda.taskpool.api.business.newCorrelations
import io.holunda.camunda.taskpool.api.task.SourceReference
import io.holunda.camunda.taskpool.api.task.WithPayload
import org.axonframework.modelling.command.TargetAggregateIdentifier
import org.axonframework.serialization.Revision
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables

/**
 * A command describing a lifecycle change of a process instance.
 */
interface ProcessInstanceCommand : WithPayload, WithCorrelations {
  /**
   * Identifies the Camunda source from which this lifecycle change originated.
   */
  val sourceReference: SourceReference

  /**
   * Identifies the process-instance aggregate receiving this command.
   */
  val processInstanceId: String

  /**
   * Whether the collector has deliberately completed payload and correlation enrichment.
   */
  var enriched: Boolean
}

/**
 * Records that a process instance started in the engine.
 */
@Revision("2")
data class StartProcessInstanceCommand(
  /**
   * Identifies the process-instance aggregate.
   */
  @TargetAggregateIdentifier override val processInstanceId: String,
  /**
   * Identifies the engine source of the process instance.
   */
  override val sourceReference: SourceReference,
  /**
   * Business key supplied when the process instance was started, if any.
   */
  override val businessKey: String? = null,
  /**
   * Identifies the user that started the process instance, if supplied by Camunda.
   */
  val startUserId: String? = null,
  /**
   * Identifies the calling super process or case instance, if any.
   */
  val superInstanceId: String? = null,
  /**
   * Identifies the activity that started this process instance, if available.
   */
  val startActivityId: String? = null,
  /**
   * Process variables selected for publication with this lifecycle change.
   */
  override val payload: VariableMap = Variables.createVariables(),
  /**
   * Business-data references derived from the selected process variables.
   */
  override val correlations: CorrelationMap = newCorrelations(),
  /**
   * Whether enrichment has been performed, including deliberately empty enrichment.
   */
  override var enriched: Boolean = false,
) : ProcessInstanceCommand

/**
 * Base type for commands that end a process instance.
 */
abstract class EndProcessInstanceCommand : ProcessInstanceCommand

/**
 * Records that a process instance finished normally or was internally terminated.
 */
@Revision("2")
data class FinishProcessInstanceCommand(
  /**
   * Identifies the process-instance aggregate.
   */
  @TargetAggregateIdentifier override val processInstanceId: String,
  /**
   * Identifies the engine source of the process instance.
   */
  override val sourceReference: SourceReference,
  /**
   * Business key associated with the process instance, if any.
   */
  override val businessKey: String? = null,
  /**
   * Identifies the calling super process or case instance, if any.
   */
  val superInstanceId: String? = null,
  /**
   * Identifies the activity at which the process instance ended, if available.
   */
  val endActivityId: String?,
  /**
   * Process variables selected for publication with this lifecycle change.
   */
  override val payload: VariableMap = Variables.createVariables(),
  /**
   * Business-data references derived from the selected process variables.
   */
  override val correlations: CorrelationMap = newCorrelations(),
  /**
   * Whether enrichment has been performed, including deliberately empty enrichment.
   */
  override var enriched: Boolean = false,
) : EndProcessInstanceCommand()

/**
 * Records that a user or external actor cancelled a process instance.
 */
@Revision("2")
data class CancelProcessInstanceCommand(
  /**
   * Identifies the process-instance aggregate.
   */
  @TargetAggregateIdentifier override val processInstanceId: String,
  /**
   * Identifies the engine source of the process instance.
   */
  override val sourceReference: SourceReference,
  /**
   * Business key associated with the process instance, if any.
   */
  override val businessKey: String? = null,
  /**
   * Identifies the calling super process or case instance, if any.
   */
  val superInstanceId: String? = null,
  /**
   * Identifies the activity at which the process instance ended, if available.
   */
  val endActivityId: String?,
  /**
   * Camunda's deletion reason for the cancellation, if supplied.
   */
  val deleteReason: String?,
  /**
   * Process variables selected for publication with this lifecycle change.
   */
  override val payload: VariableMap = Variables.createVariables(),
  /**
   * Business-data references derived from the selected process variables.
   */
  override val correlations: CorrelationMap = newCorrelations(),
  /**
   * Whether enrichment has been performed, including deliberately empty enrichment.
   */
  override var enriched: Boolean = false,
) : EndProcessInstanceCommand()

/**
 * Base type for commands that change the availability of a process instance.
 */
abstract class UpdateProcessInstanceCommand : ProcessInstanceCommand

/**
 * Records that a process instance was suspended.
 */
@Revision("2")
data class SuspendProcessInstanceCommand(
  /**
   * Identifies the process-instance aggregate.
   */
  @TargetAggregateIdentifier override val processInstanceId: String,
  /**
   * Identifies the engine source of the process instance.
   */
  override val sourceReference: SourceReference,
  /**
   * Business key associated with the process instance, if any.
   */
  override val businessKey: String? = null,
  /**
   * Process variables selected for publication with this lifecycle change.
   */
  override val payload: VariableMap = Variables.createVariables(),
  /**
   * Business-data references derived from the selected process variables.
   */
  override val correlations: CorrelationMap = newCorrelations(),
  /**
   * Whether enrichment has been performed, including deliberately empty enrichment.
   */
  override var enriched: Boolean = false,
) : UpdateProcessInstanceCommand()

/**
 * Records that a suspended process instance was resumed.
 */
@Revision("2")
data class ResumeProcessInstanceCommand(
  /**
   * Identifies the process-instance aggregate.
   */
  @TargetAggregateIdentifier override val processInstanceId: String,
  /**
   * Identifies the engine source of the process instance.
   */
  override val sourceReference: SourceReference,
  /**
   * Business key associated with the process instance, if any.
   */
  override val businessKey: String? = null,
  /**
   * Process variables selected for publication with this lifecycle change.
   */
  override val payload: VariableMap = Variables.createVariables(),
  /**
   * Business-data references derived from the selected process variables.
   */
  override val correlations: CorrelationMap = newCorrelations(),
  /**
   * Whether enrichment has been performed, including deliberately empty enrichment.
   */
  override var enriched: Boolean = false,
) : UpdateProcessInstanceCommand()
