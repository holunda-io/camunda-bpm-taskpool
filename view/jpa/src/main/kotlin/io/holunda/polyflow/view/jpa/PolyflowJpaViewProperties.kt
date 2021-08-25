package io.holunda.polyflow.view.jpa

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

/**
 * Properties to configure JPA View.
 */
@ConstructorBinding
@ConfigurationProperties(prefix = "polyflow.view.jpa")
data class PolyflowJpaViewProperties(
  val payloadAttributeLevelLimit: Int = -1,
  val eventEmittingType: EventEmittingType = EventEmittingType.AFTER_COMMIT
)

/**
 * Style for emitting events.
 */
enum class EventEmittingType {
  /**
   * Couple emitting to the after commit phase of the current transaction, executing the event delivery.
   */
  AFTER_COMMIT,

  /**
   * Couple emitting to the before commit phase of the current transaction, executing the event delivery.
   */
  BEFORE_COMMIT,

  /**
   * Direct emitting without transactional coupling.
   */
  DIRECT
}
