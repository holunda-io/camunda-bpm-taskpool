package io.holunda.camunda.taskpool.view

/**
 * Creates a complete URL to call the tasks form e.g. from the task list
 */
interface TaskUrlResolver {
  fun resolveUrl(task: Task): String
}
