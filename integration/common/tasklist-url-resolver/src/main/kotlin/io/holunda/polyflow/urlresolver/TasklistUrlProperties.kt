package io.holunda.polyflow.urlresolver

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Configuration properties for task list URL.
 */
@ConfigurationProperties(prefix = "polyflow.integration.tasklist")
data class TasklistUrlProperties(
  /**
   * URL of the task list.
   */
  val tasklistUrl: String? = null
)
