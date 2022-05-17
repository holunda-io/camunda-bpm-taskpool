package io.holunda.polyflow.urlresolver

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

/**
 * Configuration properties for task list URL.
 */
@ConfigurationProperties(prefix = "polyflow.integration.tasklist")
@ConstructorBinding
data class TasklistUrlProperties(
  /**
   * URL of the task list.
   */
  val tasklistUrl: String? = null
)
