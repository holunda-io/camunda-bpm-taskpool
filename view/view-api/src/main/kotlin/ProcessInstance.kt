package io.holunda.polyflow.view

import io.holunda.camunda.taskpool.api.task.SourceReference

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
  val deleteReason: String? = null
)


