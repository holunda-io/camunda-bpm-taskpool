package io.holunda.polyflow.taskpool.sender.task.accumulator

import io.holunda.camunda.taskpool.api.task.*

/**
 * Due to Camunda event handling implementation eventing might be slightly strange.
 * Ignore error reporting if to any original the detail is AddCandidateUsersCommand or UpdateAttributeTaskCommand, since both those commands
 * should be primary intent (original) and not detail.
 */
object EngineTaskCommandProjectionErrorDetector : ProjectionErrorDetector {

  override fun shouldReportError(original: Any, detail: Any): Boolean {
    return when {
      original !is CreateTaskCommand && detail is AddCandidateUsersCommand -> false
      original !is CreateTaskCommand && detail is DeleteCandidateUsersCommand -> false
      else -> true
    }
  }
}