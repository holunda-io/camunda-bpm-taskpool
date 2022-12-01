package io.holunda.polyflow.taskpool.sender.task.accumulator

import io.holunda.camunda.taskpool.api.task.EngineTaskCommand

/**
 * Detects intents of task command lists.
 */
interface EngineTaskCommandIntentDetector {

  /**
   * Detects intents from a list of tasks commands collected for a single user task id.
   * @param engineTaskCommands commands collected from the engine.
   * @return list of intents. Every intent is a list of corresponding task commands sorted in a way, that the intended command is the first element in the list.
   */
  fun detectIntents(engineTaskCommands: List<EngineTaskCommand>): List<List<EngineTaskCommand>>
}