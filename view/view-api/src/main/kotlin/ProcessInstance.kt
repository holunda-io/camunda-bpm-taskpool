package io.holunda.polyflow.view

import io.holunda.camunda.taskpool.api.task.SourceReference
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables

/**
 * Represents process instance.
 */
data class ProcessInstance(
  /**
   * Process instance id.
   */
  val processInstanceId: String,
  /**
   * Process source reference.
   */
  val sourceReference: SourceReference,

  /**
   * Represents instance state.
   */
  val state: ProcessInstanceState = ProcessInstanceState.RUNNING,

  /**
   * the business key of the process instance
   */
  val businessKey: String? = null,

  /**
   * the id of the super case instance
   */
  val superInstanceId: String? = null,

  /**
   * id of the activity which started the process instance
   */
  val startActivityId: String? = null,

  /**
   * the id of the user that started the process instance
   */
  val startUserId: String? = null,

  /**
   * id of the activity which ended the process instance
   */
  val endActivityId: String? = null,

  /**
   * the reason why this process instance was cancelled (deleted)
   */
  val deleteReason: String? = null,

  /**
   * Enriched lifecycle payload.
   */
  val payload: VariableMap = Variables.createVariables(),

  /**
   * Enriched lifecycle correlations.
   */
  val correlations: VariableMap = Variables.createVariables(),
) {
  /**
   * Retains the JVM constructor exposed before lifecycle payload and correlations were added.
   */
  constructor(
    legacyProcessInstanceId: String,
    legacySourceReference: SourceReference,
    legacyState: ProcessInstanceState = ProcessInstanceState.RUNNING,
    legacyBusinessKey: String? = null,
    legacySuperInstanceId: String? = null,
    legacyStartActivityId: String? = null,
    legacyStartUserId: String? = null,
    legacyEndActivityId: String? = null,
    legacyDeleteReason: String? = null,
  ) : this(
    processInstanceId = legacyProcessInstanceId,
    sourceReference = legacySourceReference,
    state = legacyState,
    businessKey = legacyBusinessKey,
    superInstanceId = legacySuperInstanceId,
    startActivityId = legacyStartActivityId,
    startUserId = legacyStartUserId,
    endActivityId = legacyEndActivityId,
    deleteReason = legacyDeleteReason,
  )

  /**
   * Retains the JVM copy function exposed before lifecycle payload and correlations were added.
   */
  fun copy(
    legacyProcessInstanceId: String = processInstanceId,
    legacySourceReference: SourceReference = sourceReference,
    legacyState: ProcessInstanceState = state,
    legacyBusinessKey: String? = businessKey,
    legacySuperInstanceId: String? = superInstanceId,
    legacyStartActivityId: String? = startActivityId,
    legacyStartUserId: String? = startUserId,
    legacyEndActivityId: String? = endActivityId,
    legacyDeleteReason: String? = deleteReason,
  ): ProcessInstance = ProcessInstance(
    processInstanceId = legacyProcessInstanceId,
    sourceReference = legacySourceReference,
    state = legacyState,
    businessKey = legacyBusinessKey,
    superInstanceId = legacySuperInstanceId,
    startActivityId = legacyStartActivityId,
    startUserId = legacyStartUserId,
    endActivityId = legacyEndActivityId,
    deleteReason = legacyDeleteReason,
    payload = payload,
    correlations = correlations,
  )
}


