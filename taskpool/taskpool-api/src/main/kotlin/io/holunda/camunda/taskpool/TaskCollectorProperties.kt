package io.holunda.camunda.taskpool

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.NestedConfigurationProperty

/**
 * Configuration properties of task collector.
 * Not using constructor binding here, because we need the springApplicationName.
 */
@ConfigurationProperties(prefix = "camunda.taskpool.collector")
class TaskCollectorProperties(

  /**
   * Denotes the (logical) name of the process application.
   */
  @Value("\${spring.application.name:unset-application-name}")
  var applicationName: String,

  /**
   * Optional tasklist url, if no explicit resolver is provided.
   */
  var tasklistUrl: String? = null,

  /**
   * Global value to control the command gateway.
   */
  var sendCommandsEnabled: Boolean = false,

  /**
   * Deprecated task enricher properties -> moved to task.enricher.
   */
  @NestedConfigurationProperty
  @Deprecated("Please use camunda.taskpool.collector.task.enricher instead")
  var enricher: TaskCollectorEnricherProperties? = null,

  /**
   * Task collector properties.
   */
  @NestedConfigurationProperty
  var task: TaskProperties = TaskProperties(),
  /**
   * Process definition collector properties.
   */
  @NestedConfigurationProperty
  var processDefinition: ProcessDefinitionProperties = ProcessDefinitionProperties(),

  /**
   * Process instance collector properties.
   */
  @NestedConfigurationProperty
  var processInstance: ProcessInstanceProperties = ProcessInstanceProperties()
)

/**
 * Task collector properties.
 * No constructor binding because of application name.
 */
@ConstructorBinding
data class TaskProperties(
  /**
   * Enable by default (since it was enabled before).
   */
  val enabled: Boolean = true,
  /**
   * Task enricher properties.
   */
  @NestedConfigurationProperty
  val enricher: TaskCollectorEnricherProperties = TaskCollectorEnricherProperties(),

  /**
   * Task sender properties.
   */
  @NestedConfigurationProperty
  val sender: TaskSenderProperties = TaskSenderProperties()
)

/**
 * Task command enricher properties.
 */
@ConstructorBinding
data class TaskCollectorEnricherProperties(
  /**
   * Type of enricher, see TaskCollectorEnricherType values.
   */
  val type: TaskCollectorEnricherType = TaskCollectorEnricherType.processVariables,
)

/**
 * Type of enricher.
 */
enum class TaskCollectorEnricherType {
  /**
   * No enrichment.
   */
  no,

  /**
   * Enrich with process variables.
   */
  processVariables,

  /**
   * Custom enricher.
   */
  custom
}


/**
 * Command sender properties.
 */
@ConstructorBinding
data class TaskSenderProperties(
  /**
   * Sender type, defaults to <code>tx</code>
   */
  val type: TaskSenderType = TaskSenderType.tx,
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
enum class TaskSenderType {
  /**
   * Sending after transaction commit.
   */
  tx,

  /**
   * Custom sending.
   */
  custom,

  /**
   * Direct sending.
   */
  simple
}

/**
 * Properties controlling the transfer of process definitions deployments.
 */
@ConstructorBinding
data class ProcessDefinitionProperties(

  /**
   * Disable by default.
   */
  val enabled: Boolean = false
)


/**
 * Properties controlling the transfer of process instance.
 */
@ConstructorBinding
data class ProcessInstanceProperties(

  /**
   * Disable by default.
   */
  val enabled: Boolean = false
)
