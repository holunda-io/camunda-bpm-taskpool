package io.holunda.camunda.taskpool.api.process.instance

import io.holunda.camunda.taskpool.api.task.SourceReference
import org.axonframework.serialization.Revision

@Revision("1")
data class ProcessInstanceStartedEvent(

  val processInstanceId: String,
  val sourceReference: SourceReference,

  /** the business key of the process instance  */
  val businessKey: String? = null,

  /** the id of the user that started the process instance  */
  val startUserId: String? = null,

  /** the id of the super case instance  */
  val superInstanceId: String? = null,

  /** id of the activity which started the process instance  */
  val startActivityId: String? = null
)

@Revision("1")
data class ProcessInstanceEndedEvent(
  val processInstanceId: String,
  val sourceReference: SourceReference,

  /** the business key of the process instance  */
  val businessKey: String? = null,

  /** the id of the super case instance  */
  val superInstanceId: String? = null,

  /** id of the activity which ended the process instance */
  val endActivityId: String?,

  /** the reason why this process instance was cancelled (deleted) */
  val deleteReason: String?
)
