package io.holunda.polyflow.taskpool.collector

import io.holunda.polyflow.spring.ApplicationNameBeanPostProcessor.Companion.UNSET_APPLICATION_NAME
import io.holunda.polyflow.taskpool.collector.task.assigner.ProcessVariableTaskAssignerMapping
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty

/**
 * Configuration properties of the Process Engine API Taskpool collector.
 */
@ConfigurationProperties(prefix = "polyflow.integration.collector.processengineapi")
class ProcessEngineApiTaskpoolCollectorProperties(

  /**
   * Denotes the (logical) name of the process application, defaults to "spring.application.name".
   */
  // The default is set by ApplicationNameBeanPostProcessor
  var applicationName: String = UNSET_APPLICATION_NAME,

  /**
   * Task collector properties.
   */
  @NestedConfigurationProperty
  var task: ProcessEngineApiTaskCollectorProperties = ProcessEngineApiTaskCollectorProperties(),
)

/**
 * Task collector properties.
 */
data class ProcessEngineApiTaskCollectorProperties(
  /**
   * Task enricher properties.
   */
  @NestedConfigurationProperty
  val enricher: TaskCollectorEnricherProperties = TaskCollectorEnricherProperties(),

  /**
   * Task assigner properties.
   */
  @NestedConfigurationProperty
  val assigner: TaskAssignerProperties = TaskAssignerProperties(),

  /**
   * Flag to enable or disable the collector.
   */
  val enabled: Boolean = true,
)


/**
 * Task command enricher properties.
 */
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
 * Type of task assigner.
 */
enum class TaskAssignerType {
  /**
   * Empty assigner, use information from Camunda task.
   */
  no,

  /**
   * Use process variables for assignment information.
   */
  processVariables,

  /**
   * Custom assigner.
   */
  custom
}



/**
 * Properties to set up the task assigner.
 */
data class TaskAssignerProperties(
  /**
   * Configures assigner type.
   */
  val type: TaskAssignerType = TaskAssignerType.no,
  /**
   * Process variable carrying the assignee information used by the process variable task assigner.
   */
  val assignee: String? = null,
  /**
   * Process variable carrying the candidateUsers information used by the process variable task assigner.
   */
  val candidateUsers: String? = null,
  /**
   * Process variable carrying the candidateGroups information used by the process variable task assigner.
   */
  val candidateGroups: String? = null
) {
  /**
   * Constructs mapping from properties.
   */
  fun toMapping(): ProcessVariableTaskAssignerMapping = ProcessVariableTaskAssignerMapping(
    assignee = assignee,
    candidateUsers = candidateUsers,
    candidateGroups = candidateGroups,
  )
}
