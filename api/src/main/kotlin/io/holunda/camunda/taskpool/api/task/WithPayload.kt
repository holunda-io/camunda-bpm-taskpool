package io.holunda.camunda.taskpool.api.task

interface WithPayload {
  val payload: MutableMap<String, Any>
  val businessKey: String?
  var enriched: Boolean
}
