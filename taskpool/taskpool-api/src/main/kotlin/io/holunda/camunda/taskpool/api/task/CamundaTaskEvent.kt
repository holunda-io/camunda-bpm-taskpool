package io.holunda.camunda.taskpool.api.task

/**
 * Identifies camunda task event.
 */
interface CamundaTaskEvent {
  val eventName: String

  companion object {
    const val CREATE = "create"
    const val ASSIGN = "assign"
    const val DELETE = "delete"
    const val COMPLETE = "complete"
    const val ATTRIBUTES = "attribute-update"
    const val CANDIDATE_GROUP_ADD = "candidate-group-add"
    const val CANDIDATE_GROUP_DELETE = "candidate-group-delete"
    const val CANDIDATE_USER_ADD = "candidate-user-add"
    const val CANDIDATE_USER_DELETE = "candidate-user-delete"
  }
}
