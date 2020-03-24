package io.holunda.camunda.taskpool.cockpit.service

/**
 * Query for all tasks.
 */
data class QueryTaskEvents(val ids: List<String> = listOf()) {
  /**
   * applicable to what subscription?
   */
  fun apply(id: String) = ids.isEmpty() || ids.contains(id)
}
