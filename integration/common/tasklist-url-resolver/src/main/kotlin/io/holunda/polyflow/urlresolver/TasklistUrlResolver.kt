package io.holunda.polyflow.urlresolver

/**
 * Resolver for tasklist URL.
 */
interface TasklistUrlResolver {
  /**
   * Retrieves the URL of the task list application.
   * @return task list URL
   */
  fun getTasklistUrl() : String
}
