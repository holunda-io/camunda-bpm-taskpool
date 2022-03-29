package io.holunda.polyflow.view.mongo

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.NestedConfigurationProperty

/**
 * Configures mongo projection mode.
 */
@ConfigurationProperties(prefix = "polyflow.view.mongo")
@ConstructorBinding
data class TaskPoolMongoViewProperties(
  /**
   * Tracking mode.
   */
  val changeTrackingMode: ChangeTrackingMode = ChangeTrackingMode.EVENT_HANDLER,

  @NestedConfigurationProperty
  val indexes: Indexes = Indexes()
)

/**
 * Defines which indexes are created by default.
 */
@ConstructorBinding
data class Indexes(
  /**
   * Controls the index of the token store.
   */
  val tokenStore: Boolean = true
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
