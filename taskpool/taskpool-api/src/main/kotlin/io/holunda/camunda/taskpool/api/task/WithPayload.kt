package io.holunda.camunda.taskpool.api.task

import org.camunda.bpm.engine.variable.VariableMap

/**
 * Represents task payload.
 */
interface WithPayload {
  /**
   * Payload.
   */
  val payload: VariableMap
  /**
   * Business key.
   */
  val businessKey: String?
}
