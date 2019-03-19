package io.holunda.camunda.taskpool.urlresolver

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "camunda.taskpool.form-url-resolver")
data class FormUrlResolverProperties(
  var defaultTaskTemplate: String = "",
  var defaultApplicationTemplate: String = "",
  var defaultProcessTemplate: String = "",

  var applications: Map<String, Application> = mutableMapOf()

) {

  fun getTaskUrlTemplate(applicationName: String, taskDefinitionKey: String): String {
    val application = applications[applicationName] ?: return defaultTaskTemplate

    return application.tasks.getOrDefault(taskDefinitionKey, defaultTaskTemplate)
  }

  fun getProcessUrlTemplate(applicationName: String, processDefinitionKey: String): String {
    val application = applications[applicationName] ?: return defaultProcessTemplate
    return application.processes.getOrDefault(processDefinitionKey, defaultProcessTemplate)
  }


  fun getApplicationTemplate(applicationName: String): String {
    val application = applications[applicationName] ?: return defaultApplicationTemplate

    return application.url ?: defaultApplicationTemplate
  }


  data class Application(
    var url: String? = null,
    var tasks: Map<String, String> = mutableMapOf(),
    var processes: Map<String, String> = mutableMapOf()
  )
}
