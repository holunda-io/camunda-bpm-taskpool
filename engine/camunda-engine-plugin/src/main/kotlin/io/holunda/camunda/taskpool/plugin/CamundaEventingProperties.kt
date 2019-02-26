package io.holunda.camunda.taskpool.plugin

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Configuration properties or Camunda Eventing plugin
 */
@ConfigurationProperties(prefix = "camunda.taskpool.engine-eventing")
data class CamundaEventingProperties(
  /**
   * Enable eventing (false by default).
   */
  var enabled: Boolean = false,
  /**
   * Enable task lifecycle events (true by default).
   */
  var taskEventing: Boolean = true,
  /**
   * Enable execution lifecycle events (true by default).
   */
  var executionEventing: Boolean = true,
  /**
   * Enable hostoric eventing (true by default).
   */
  var historicEventing: Boolean = true
)


