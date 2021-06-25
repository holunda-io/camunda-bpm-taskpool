package io.holunda.polyflow.view.mongo

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Configures mongo projection mode.
 */
@ConfigurationProperties(prefix = "polyflow.view.mongo")
data class TaskPoolMongoViewProperties(
  /**
   * Tracking mode.
   */
  var changeTrackingMode: io.holunda.polyflow.view.mongo.ChangeTrackingMode = io.holunda.polyflow.view.mongo.ChangeTrackingMode.EVENT_HANDLER
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
