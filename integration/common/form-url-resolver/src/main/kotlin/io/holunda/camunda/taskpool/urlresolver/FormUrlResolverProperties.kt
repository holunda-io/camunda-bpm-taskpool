package io.holunda.camunda.taskpool.urlresolver

import io.holunda.camunda.taskpool.api.business.EntryType
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.NestedConfigurationProperty

/**
 * Properties responsible for resolution of form URL.
 */
@ConfigurationProperties(prefix = "polyflow.integration.form-url-resolver")
@ConstructorBinding
data class FormUrlResolverProperties(
  val defaultTaskTemplate: String = "",
  val defaultApplicationTemplate: String = "",
  val defaultProcessTemplate: String = "",
  val defaultDataEntryTemplate: String = "",

  @NestedConfigurationProperty
  val applications: Map<String, Application> = mutableMapOf()
) {

  /**
   * Retrieves URL template for User Task.
   * @param applicationName application name.
   * @param taskDefinitionKey definition key of the user task.
   * @return template.
   */
  fun getTaskUrlTemplate(applicationName: String, taskDefinitionKey: String): String {
    val application = applications[applicationName] ?: return defaultTaskTemplate
    return application.tasks.getOrDefault(taskDefinitionKey, defaultTaskTemplate)
  }

  /**
   * Retrieves URL template for Process Start Form.
   * @param applicationName application name.
   * @param processDefinitionKey definition key of the process.
   * @return template.
   */
  fun getProcessUrlTemplate(applicationName: String, processDefinitionKey: String): String {
    val application = applications[applicationName] ?: return defaultProcessTemplate
    return application.processes.getOrDefault(processDefinitionKey, defaultProcessTemplate)
  }

  /**
   * Retrieves URL template for Data Entry.
   * @param applicationName application name.
   * @param entryType type of data entry.
   * @return template.
   */
  fun getDataEntryTemplate(applicationName: String, entryType: EntryType): String {
    val application = applications[applicationName] ?: return defaultDataEntryTemplate
    return application.dataEntries.getOrDefault(entryType, defaultDataEntryTemplate)
  }

  /**
   * Retrieves URL template for the process application.
   * @param applicationName application name.
   * @return template.
   */
  fun getApplicationTemplate(applicationName: String): String {
    val application = applications[applicationName] ?: return defaultApplicationTemplate
    return application.url ?: defaultApplicationTemplate
  }

  @ConstructorBinding
  data class Application(
    val url: String? = null,
    val tasks: Map<String, String> = mutableMapOf(),
    val processes: Map<String, String> = mutableMapOf(),
    val dataEntries: Map<String, String> = mutableMapOf()
  )
}
