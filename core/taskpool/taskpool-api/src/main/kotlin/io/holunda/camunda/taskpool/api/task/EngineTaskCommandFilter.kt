package io.holunda.camunda.taskpool.api.task

import java.util.function.Predicate

/**
 * Filter for task commands.
 */
interface EngineTaskCommandFilter : Predicate<EngineTaskCommand> {
  /**
   * Tests if the task command should be sent.
   * @return true, if command should be emitted. Defaults to false.
   */
  override fun test(t: EngineTaskCommand): Boolean = false
}
