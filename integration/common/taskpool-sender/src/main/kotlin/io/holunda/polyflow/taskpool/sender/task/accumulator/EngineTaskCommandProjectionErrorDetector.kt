package io.holunda.polyflow.taskpool.sender.task.accumulator

import io.holunda.camunda.taskpool.api.task.*

/**
 * Due to Camunda event handling implementation eventing might be slightly strange.
 * Ignore error reporting if:
 * - to any original (intent) the detail is UpdateAttributesHistoricTaskCommand
 * - to any original except create the detail is AddCandidateUsersCommand, DeleteCandidateUsersCommand, since the detail should be detected as primary intent
 */
object EngineTaskCommandProjectionErrorDetector : ProjectionErrorDetector {

  override fun shouldReportError(original: Any, detail: Any): Boolean {
    return when {
      original !is CreateTaskCommand && detail is AddCandidateUsersCommand -> false
      original !is CreateTaskCommand && detail is DeleteCandidateUsersCommand -> false
      detail is UpdateAttributesHistoricTaskCommand -> false
      else -> true
    }
  }
}
