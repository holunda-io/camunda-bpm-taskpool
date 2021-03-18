package io.holunda.camunda.taskpool.api.business

import org.camunda.bpm.engine.variable.VariableMap

/**
 * Represents data payload.
 */
interface WithPayload {
  /**
   * Payload.
   */
  val payload: VariableMap
}
