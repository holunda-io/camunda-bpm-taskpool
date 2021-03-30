package io.holunda.camunda.taskpool

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.NestedConfigurationProperty

/**
 * Command sender properties.
 */
@ConfigurationProperties(prefix="polyflow.integration.sender")
@ConstructorBinding
data class SenderProperties(
  /**
   * Global value to control the command gateway.
   */
  var enabled: Boolean = true,
  /**
   * Sending tasks.
   */
  @NestedConfigurationProperty
  val task: TaskSenderProperties = TaskSenderProperties(),
  /**
   * Sending process definitions.
   */
  @NestedConfigurationProperty
  val processDefinition: ProcessDefinitionSenderProperties = ProcessDefinitionSenderProperties(),
  /**
   * Sending process instances.
   */
  @NestedConfigurationProperty
  val processInstance: ProcessInstanceSenderProperties = ProcessInstanceSenderProperties(),
)


@ConstructorBinding
data class TaskSenderProperties(
  /**
   * Value to control the task sending.
   */
  val enabled: Boolean = true,
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

@ConstructorBinding
data class ProcessDefinitionSenderProperties(
  /**
   * Value to control the process definition sending.
   */
  val enabled: Boolean = false,
  /**
   * Sender type, defaults to <code>simple</code>
   */
  val type: SenderType = SenderType.simple,
)

@ConstructorBinding
data class ProcessInstanceSenderProperties(
  /**
   * Value to control the process instance sending.
   */
  val enabled: Boolean = true,
  /**
   * Sender type, defaults to <code>simple</code>
   */
  val type: SenderType = SenderType.simple,
)

@ConstructorBinding
data class ProcessVariableSenderProperties(
  /**
   * Value to control the process variable sending.
   */
  val enabled: Boolean = true,
  /**
   * Sender type, defaults to <code>simple</code>
   */
  val type: SenderType = SenderType.simple,
)

/**
 * Sender type.
 */
enum class SenderType {
  /**
   * Direct sending.
   */
  simple,
  /**
   * Sending using Tx synchronization.
   */
  tx,
  /**
   * Custom sending.
   */
  custom
}
