package io.holunda.camunda.taskpool.api.task

interface PayloadCommand {
  val payload: MutableMap<String, Any>
  var enriched: Boolean
}
