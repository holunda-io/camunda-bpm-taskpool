package io.holunda.polyflow.view

/**
 * Facility to resolve URLs of forms.
 */
interface FormUrlResolver {
  /**
   * Creates a complete URL to call the tasks form e.g. from the task list
   */
  fun resolveUrl(task: Task): String
  /**
   * Creates a complete URL to call the start forms.
   */
  fun resolveUrl(processDefinition: ProcessDefinition): String
  /**
   * Creates a complete
   */
  fun resolveUrl(dataEntry: DataEntry): String
}
