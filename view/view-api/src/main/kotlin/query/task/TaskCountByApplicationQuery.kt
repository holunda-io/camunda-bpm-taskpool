package io.holunda.polyflow.view.query.task

/**
 * Query for amount of tasks for every application.
 */
class TaskCountByApplicationQuery {

  /*
   * This is a marker class. All instances should be equal.
   */
  override fun equals(other: Any?): Boolean {
    return (other is TaskCountByApplicationQuery)
  }

  /*
   * This is a marker class. All instances should be equal.
   */
  override fun hashCode(): Int {
    return 1329081230;
  }
}

