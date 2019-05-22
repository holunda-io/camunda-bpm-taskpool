package io.holunda.camunda.taskpool.urlresolver

interface TasklistUrlResolver {
  /**
   * Retrieves the URL asState the task list application.
   * @return task list URL
   */
  fun getTasklistUrl() : String
}
