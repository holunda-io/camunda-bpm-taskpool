package io.holunda.camunda.taskpool.example.tasklist

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "camunda.taskpool.url-resolver")
data class TaskUrlResolverProperties(
  var default: String = "",
  var tasks: Map<String, String> = mutableMapOf()
) {
  fun getUrlTemplate(taskDefinitionKey: String) = tasks.getOrDefault(taskDefinitionKey, default)
}
