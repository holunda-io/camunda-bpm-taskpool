package io.holunda.polyflow.view.query.task

/**
 * Query for amount of tasks for every application.
 */
class TaskCountByApplicationQuery

/**
 * Response type.
 * FIXME: think of encapsulating it into a typed result instead of List<ApplicationWithTaskCount>
 */
data class ApplicationWithTaskCount(
  val application: String,
  val taskCount: Int
)
