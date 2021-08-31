package io.holunda.polyflow.view.query.task

/**
 * Information about amount of tasks per application.
 */
data class ApplicationWithTaskCount(
  val application: String,
  val taskCount: Int
)
