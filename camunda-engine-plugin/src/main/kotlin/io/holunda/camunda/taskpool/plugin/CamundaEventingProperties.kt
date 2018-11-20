package io.holunda.camunda.taskpool.plugin

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "camunda.taskpool.engine-eventing")
data class CamundaEventingProperties(
  var enabled: Boolean = false,
  var taskEventing: Boolean = true,
  var executionEventing: Boolean = true,
  var historicEventing: Boolean = true
)


