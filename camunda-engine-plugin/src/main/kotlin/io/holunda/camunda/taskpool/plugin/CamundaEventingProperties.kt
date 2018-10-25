package io.holunda.camunda.taskpool.plugin

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "camunda.taskpool.engine-eventing")
data class CamundaEventingProperties(
  var enabled: Boolean = false
)
