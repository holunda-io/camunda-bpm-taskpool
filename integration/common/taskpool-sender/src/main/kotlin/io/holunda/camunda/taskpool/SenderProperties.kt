package io.holunda.camunda.taskpool

import org.springframework.boot.context.properties.ConstructorBinding

/**
 * Command sender properties.
 */
@ConstructorBinding
data class SenderProperties(
  /**
   * Global value to control the command gateway.
   */
  var enabled: Boolean = false,
  /**
   * Sender type, defaults to <code>tx</code>
   */
  val type: SenderType = SenderType.tx,
  /**
   * This flag controls if the tasks are sent within an open transaction (value true, before commit)
   * or not (value false, default, after commit). This setting is required if you move the command bus
   * and the command handling on the engine side.
   */
  val sendWithinTransaction: Boolean = false
)

/**
 * Sender type.
 */
enum class SenderType {
  /**
   * Direct sending.
   */
  sipmple,

  /**
   * Sending using Tx synchronization.
   */
  tx,

  /**
   * Custom sending.
   */
  custom
}
