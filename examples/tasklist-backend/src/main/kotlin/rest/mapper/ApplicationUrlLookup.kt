package io.holunda.camunda.taskpool.example.tasklist.rest.mapper


/**
 * Allows to lookup an application endpoint by application-name.
 */
interface ApplicationUrlLookup {
  fun lookup(appName: String) : String
}


class DefaultApplicationUrlLookup : ApplicationUrlLookup {
  override fun lookup(appName: String): String = "http://localhost:8080/$appName"
}
