package io.holunda.camunda.taskpool.api.process.instance

import io.holunda.camunda.taskpool.api.business.CorrelationMap
import io.holunda.camunda.taskpool.api.business.WithCorrelations
import io.holunda.camunda.taskpool.api.business.newCorrelations
import io.holunda.camunda.taskpool.api.task.SourceReference
import io.holunda.camunda.taskpool.api.task.WithPayload
import org.axonframework.serialization.Revision
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables

/**
 * An immutable event describing a process-instance lifecycle change.
 */
interface ProcessInstanceEvent : WithPayload, WithCorrelations

/**
 * Published when a process instance starts.
 */
@Revision("2")
data class ProcessInstanceStartedEvent(
  /**
   * Identifies the process instance.
   */
  val processInstanceId: String,
  /**
   * Identifies the engine source of the process instance.
   */
  val sourceReference: SourceReference,
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
) : ProcessInstanceEvent

/**
 * Published when a process instance finishes normally or is internally terminated.
 */
@Revision("3")
data class ProcessInstanceEndedEvent(
  /**
   * Identifies the process instance.
   */
  val processInstanceId: String,
  /**
   * Identifies the engine source of the process instance.
   */
  val sourceReference: SourceReference,
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
) : ProcessInstanceEvent

/**
 * Published when a user or external actor cancels a process instance.
 */
@Revision("2")
data class ProcessInstanceCancelledEvent(
  /**
   * Identifies the process instance.
   */
  val processInstanceId: String,
  /**
   * Identifies the engine source of the process instance.
   */
  val sourceReference: SourceReference,
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
) : ProcessInstanceEvent

/**
 * Published when a process instance is suspended.
 */
@Revision("2")
data class ProcessInstanceSuspendedEvent(
  /**
   * Identifies the process instance.
   */
  val processInstanceId: String,
  /**
   * Identifies the engine source of the process instance.
   */
  val sourceReference: SourceReference,
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
) : ProcessInstanceEvent

/**
 * Published when a suspended process instance is resumed.
 */
@Revision("2")
data class ProcessInstanceResumedEvent(
  /**
   * Identifies the process instance.
   */
  val processInstanceId: String,
  /**
   * Identifies the engine source of the process instance.
   */
  val sourceReference: SourceReference,
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
) : ProcessInstanceEvent
