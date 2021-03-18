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
  @Deprecated("Please use camunda.taskpool.collector.task.sender.sendCommandsEnabled instead")
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
   * Task enricher properties.
   */
  @NestedConfigurationProperty
  val enricher: TaskCollectorEnricherProperties = TaskCollectorEnricherProperties(),

  /**
   * Task sender properties.
   */
  @NestedConfigurationProperty
  val sender: SenderProperties = SenderProperties()
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
