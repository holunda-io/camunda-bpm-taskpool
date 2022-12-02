package io.holunda.camunda.taskpool.api.task

/**
 * Identifies camunda task event.
 */
interface CamundaTaskEventType {
  /**
   * Event classification.
   */
  val eventName: String

  companion object {
    const val CREATE = "create"
    const val ASSIGN = "assignment"
    const val DELETE = "delete"
    const val COMPLETE = "complete"
    const val ATTRIBUTES = "attribute-update"
    const val ATTRIBUTES_LISTENER_UPDATE = "attribute-listener-update"
    const val CANDIDATE_GROUP_ADD = "candidate-group-add"
    const val CANDIDATE_GROUP_DELETE = "candidate-group-delete"
    const val CANDIDATE_USER_ADD = "candidate-user-add"
    const val CANDIDATE_USER_DELETE = "candidate-user-delete"
  }
}
