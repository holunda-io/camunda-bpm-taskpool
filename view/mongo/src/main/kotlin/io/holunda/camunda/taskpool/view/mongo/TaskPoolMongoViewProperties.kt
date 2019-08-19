package io.holunda.camunda.taskpool.view.mongo

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Configures mongo projection mode.
 */
@ConfigurationProperties(prefix = "camunda.taskpool.view.mongo")
data class TaskPoolMongoViewProperties(
  /**
   * Tracking mode.
   */
  var changeTrackingMode: ChangeTrackingMode = ChangeTrackingMode.EVENT_HANDLER
)

/**
 * Change tracking mode.
 */
enum class ChangeTrackingMode {
  /**
   * Use axon query bus and update subscriptions.
   */
  EVENT_HANDLER,
  /**
   * Use mongo change stream.
   */
  CHANGE_STREAM,
  /**
   * Disable updates.
   */
  NONE
}
