package io.holunda.camunda.taskpool.urlresolver

import io.holunda.camunda.taskpool.api.business.EntryType
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "polyflow.integration.form-url-resolver")
data class FormUrlResolverProperties(
  var defaultTaskTemplate: String = "",
  var defaultApplicationTemplate: String = "",
  var defaultProcessTemplate: String = "",
  var defaultDataEntryTemplate: String = "",

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

  fun getDataEntryTemplate(applicationName: String, entryType: EntryType): String {
    val application = applications[applicationName] ?: return defaultDataEntryTemplate
    return application.dataEntries.getOrDefault(entryType, defaultDataEntryTemplate)
  }

  fun getApplicationTemplate(applicationName: String): String {
    val application = applications[applicationName] ?: return defaultApplicationTemplate

    return application.url ?: defaultApplicationTemplate
  }

  data class Application(
    var url: String? = null,
    var tasks: Map<String, String> = mutableMapOf(),
    var processes: Map<String, String> = mutableMapOf(),
    var dataEntries: Map<String, String> = mutableMapOf()
  )
}
