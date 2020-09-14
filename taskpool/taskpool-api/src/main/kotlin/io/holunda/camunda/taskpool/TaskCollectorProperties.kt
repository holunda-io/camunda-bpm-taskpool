package io.holunda.camunda.taskpool

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.NestedConfigurationProperty
import java.lang.IllegalStateException

/**
 * Configuration properties of task collector.
 */
@ConfigurationProperties(prefix = "camunda.taskpool.collector")
class TaskCollectorProperties(
  @Value("\${spring.application.name:unset-application-name}")
  springApplicationName: String,

  /**
   * Optional tasklist url, if no explicit resolver is provided.
   */
  var tasklistUrl: String? = null,
  /**
   * Sender properties.
   */
  @NestedConfigurationProperty
  var sender: TaskSenderProperties = TaskSenderProperties(),

  /**
   * Enricher properties.
   */
  @NestedConfigurationProperty
  var enricher: TaskCollectorEnricherProperties = TaskCollectorEnricherProperties(applicationName = springApplicationName),

  /**
   * Process definition collection properties.
   */
  @NestedConfigurationProperty
  var process: ProcessDefinitionProperties = ProcessDefinitionProperties()
)

/**
 * Task command enricher properties.
 */
data class TaskCollectorEnricherProperties(
  /**
   * Type of enricher, see TaskCollectorEnricherType values.
   */
  var type: TaskCollectorEnricherType = TaskCollectorEnricherType.processVariables,
  /**
   * Denotes the (logical) name of the process application.
   */
  var applicationName: String
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
 * Task command sender properties.
 */
@ConstructorBinding
data class TaskSenderProperties(
  /**
   * Task sender enabled. Defaults to false.
   */
  var enabled: Boolean = false,
  /**
   * Sender type, defaults to <code>tx</code>
   */
  var type: TaskSenderType = TaskSenderType.tx,
  /**
   * This flag controls if the tasks are sent within an open transaction (value true, before commit)
   * or not (value false, default, after commit). This setting is required if you move the command bus
   * and the command handling on the engine side.
   */
  var sendWithinTransaction: Boolean = false
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
  var enabled: Boolean = false
)

