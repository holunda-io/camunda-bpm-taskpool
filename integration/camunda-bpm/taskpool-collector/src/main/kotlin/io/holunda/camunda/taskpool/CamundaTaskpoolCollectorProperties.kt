package io.holunda.camunda.taskpool

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.NestedConfigurationProperty

/**
 * Configuration properties of Camunda Taskpool collector.
 * Not using constructor binding here, because we need the springApplicationName.
 */
@ConfigurationProperties(prefix = "polyflow.integration.collector.camunda")
class CamundaTaskpoolCollectorProperties(

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
   * Task collector properties.
   */
  @NestedConfigurationProperty
  var task: CamundaTaskCollectorProperties = CamundaTaskCollectorProperties(),
  /**
   * Process definition collector properties.
   */
  @NestedConfigurationProperty
  var processDefinition: CamundaProcessDefinitionCollectorProperties = CamundaProcessDefinitionCollectorProperties(),

  /**
   * Process instance collector properties.
   */
  @NestedConfigurationProperty
  var processInstance: CamundaProcessInstanceCollectorProperties = CamundaProcessInstanceCollectorProperties()
)

/**
 * Task collector properties.
 * No constructor binding because of application name.
 */
@ConstructorBinding
data class CamundaTaskCollectorProperties(
  /**
   * Task enricher properties.
   */
  @NestedConfigurationProperty
  val enricher: TaskCollectorEnricherProperties = TaskCollectorEnricherProperties(),
  /**
   * Flag to enable or disable the collector.
   */
  val enabled: Boolean = true
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
data class CamundaProcessDefinitionCollectorProperties(

  /**
   * Disable by default.
   */
  val enabled: Boolean = false
)


/**
 * Properties controlling the transfer of process instance.
 */
@ConstructorBinding
data class CamundaProcessInstanceCollectorProperties(

  /**
   * Disable by default.
   */
  val enabled: Boolean = false
)
