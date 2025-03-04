package io.holunda.polyflow.taskpool.sender

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty

/**
 * Command sender properties.
 */
@ConfigurationProperties(prefix = "polyflow.integration.sender")
data class SenderProperties(
  /**
   * Global value to control the command gateway.
   */
  val enabled: Boolean = true,
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
  /**
   * Sending process instances.
   */
  @NestedConfigurationProperty
  val processVariable: ProcessVariableSenderProperties = ProcessVariableSenderProperties(),
)

/**
 * Task sender properties.
 */
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
  val sendWithinTransaction: Boolean = false,
  /**
   * Serialize payload to `Map<String, Object>`. Defaults to true.
   */
  val serializePayload: Boolean = true,
)

/**
 *  Process definition sender properties.
 */
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

/**
 * Process instance sender properties.
 */
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

/**
 * Process variable sender properties.
 */
data class ProcessVariableSenderProperties(
  /**
   * Value to control the process variable sending.
   */
  val enabled: Boolean = true,
  /**
   * Sender type, defaults to <code>simple</code>
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
   * Direct sending without accumulation.
   */
  simple,

  /**
   * Sender using Tx synchronization sending commands directly.
   */
  tx,

  /**
   * Special sender using Tx synchronization sending commands from a Camunda Job. See polyflow-camunda-bpm-taskpool-job-sender.
   */
  txjob,

  /**
   * Custom sending.
   */
  custom
}
