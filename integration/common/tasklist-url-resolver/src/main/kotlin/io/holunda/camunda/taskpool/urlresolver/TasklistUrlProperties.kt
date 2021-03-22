package io.holunda.camunda.taskpool.urlresolver

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "polyflow.integration.tasklist")
@ConstructorBinding
data class TasklistUrlProperties(
  val tasklistUrl: String? = null
)
