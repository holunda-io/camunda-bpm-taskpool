package io.holunda.camunda.taskpool.api.task

import org.camunda.bpm.engine.variable.VariableMap

/**
 * Represents task payload.
 */
interface WithPayload {
  val payload: VariableMap
  val businessKey: String?
  var enriched: Boolean
}
