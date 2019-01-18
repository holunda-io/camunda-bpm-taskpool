package io.holunda.camunda.taskpool.urlresolver

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "camunda.taskpool.task-url-resolver")
data class TaskUrlResolverProperties(
  var defaultTaskTemplate: String = "",
  var defaultApplicationTemplate: String = "",

  var applications: Map<String, Application> = mutableMapOf()

) {

  fun getUrlTemplate(applicationName: String, taskDefinitionKey: String): String {
    val application = applications[applicationName] ?: return defaultTaskTemplate

    return application.tasks.getOrDefault(taskDefinitionKey, defaultTaskTemplate)
  }

  fun getApplicationTemplate(applicationName: String): String {
    val application = applications[applicationName] ?: return defaultApplicationTemplate

    return application.url ?: defaultApplicationTemplate
  }


  data class Application(
    var url: String? = null,
    var tasks: Map<String, String> = mutableMapOf()
  )
}
