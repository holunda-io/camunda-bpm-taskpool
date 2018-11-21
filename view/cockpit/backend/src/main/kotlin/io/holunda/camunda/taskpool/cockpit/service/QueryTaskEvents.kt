package io.holunda.camunda.taskpool.cockpit.service

data class QueryTaskEvents(val ids: List<String> = listOf()) {

  fun apply(id: String) = ids.isEmpty() || ids.contains(id)
}
