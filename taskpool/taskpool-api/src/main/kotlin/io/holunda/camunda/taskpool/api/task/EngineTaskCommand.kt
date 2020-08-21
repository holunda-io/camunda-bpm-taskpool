package io.holunda.camunda.taskpool.api.task

/**
 * Task command received from the Camunda Engine.
 */
interface EngineTaskCommand : WithTaskId, CamundaTaskEvent {
  /**
   * Used to order commands before sending in case multiple events are received from the engine for the same task in the same transaction.
   * Commands with lower order value are sent before commands with higher order value.
   */
  val order: Int
}


