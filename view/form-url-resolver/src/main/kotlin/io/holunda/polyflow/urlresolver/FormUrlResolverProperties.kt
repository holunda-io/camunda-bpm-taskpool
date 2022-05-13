package io.holunda.polyflow.urlresolver

import io.holunda.camunda.taskpool.api.business.EntryType
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.NestedConfigurationProperty

/**
 * Properties responsible for resolution of form URL.
 * On the top level, the defaults are provided.
 */
@ConfigurationProperties(prefix = "polyflow.integration.form-url-resolver")
@ConstructorBinding
data class FormUrlResolverProperties(
  /**
   * URL template for the task as default.
   */
  val defaultTaskTemplate: String = "",
  /**
   * URL template for application as default.
   */
  val defaultApplicationTemplate: String = "",
  /**
   * URL template for process starter form as default.
   */
  val defaultProcessTemplate: String = "",
  /**
   * URL template for data entry form as default.
   */
  val defaultDataEntryTemplate: String = "",

  /**
   * Application-specific configuration.
   */
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

  /**
   * Represents an application configuration.
   */
  @ConstructorBinding
  data class Application(
    /**
     * Base URL template of the application.
     */
    val url: String? = null,
    /**
     * Task URL configuration for this application, keyed by taskDefinitionId. The value represents the URL template.
     */
    val tasks: Map<String, String> = mutableMapOf(),
    /**
     * Processes URL configuration for this application keyed by the processDefinitionKey. The value represents the URL template.
     */
    val processes: Map<String, String> = mutableMapOf(),
    /**
     * Data entry URL configuration for this application keyed by the dataEntryType. The value represents the URL template.
     */
    val dataEntries: Map<String, String> = mutableMapOf()
  )
}
