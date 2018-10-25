package io.holunda.camunda.taskpool.example.tasklist.rest.mapper

import io.holunda.camunda.taskpool.view.Task

/**
 * Creates a complete REST URL to call the tasks form.
 */
interface TaskUrlResolver {

  fun resolveUrl(task: Task) : String

}

/**
 * Allows to lookup an application endpoint by application-name.
 */
interface ApplicationUrlLookup {

  fun lookup(appName: String) : String
}
