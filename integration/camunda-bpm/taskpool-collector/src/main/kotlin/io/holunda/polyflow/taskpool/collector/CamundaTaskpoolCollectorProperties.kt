package io.holunda.polyflow.taskpool.collector

import io.holunda.polyflow.spring.ApplicationNameBeanPostProcessor.Companion.UNSET_APPLICATION_NAME
import io.holunda.polyflow.taskpool.collector.task.assigner.ProcessVariableTaskAssignerMapping
import io.holunda.polyflow.taskpool.collector.task.enricher.FilterType
import io.holunda.polyflow.taskpool.collector.task.enricher.ProcessVariableCorrelation
import io.holunda.polyflow.taskpool.collector.task.enricher.ProcessVariableFilter
import io.holunda.polyflow.taskpool.collector.task.enricher.TaskVariableFilter
import io.holunda.polyflow.taskpool.collector.task.enricher.VariableFilter
import org.camunda.bpm.engine.delegate.TaskListener
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty

/**
 * Configuration properties of Camunda Taskpool collector.
 */
@ConfigurationProperties(prefix = "polyflow.integration.collector.camunda")
class CamundaTaskpoolCollectorProperties(

  /**
   * Denotes the (logical) name of the process application, defaults to "spring.application.name".
   */
  // The default is set by ApplicationNameBeanPostProcessor
  var applicationName: String = UNSET_APPLICATION_NAME,

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
  var processInstance: CamundaProcessInstanceCollectorProperties = CamundaProcessInstanceCollectorProperties(),

  /**
   * Process variable collector properties.
   */
  @NestedConfigurationProperty
  var processVariable: CamundaProcessVariableProperties = CamundaProcessVariableProperties()
)

/**
 * Task collector properties.
 */
data class CamundaTaskCollectorProperties(
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

  /**
   * Properties of task importer.
   */
  @NestedConfigurationProperty
  val importer: TaskImporterProperties = TaskImporterProperties(),

  /**
   * List of task events to be excluded from collector. Defaults to empty list, so all events are collected.
   * Possible values are constants defined in [TaskListener].
   */
  val excludedTaskEventNames: List<String> = listOf(),

  /**
   * List of history events to restrict (HistoricTaskInstanceEventEntity, HistoricIdentityLinkLogEventEntity). Defaults to empty list, so all events are collected.
   * Possible values are constants defined in [org.camunda.bpm.engine.impl.history.event.HistoryEventTypes] + "update".
   */
  val excludedHistoryEventNames: List<String> = listOf()
) {
  /**
   * Determines if the provided event name should be collected.
   * @param eventName event name to check.
   * @return true if not excluded.
   */
  fun collectTaskEvent(eventName: String): Boolean = !excludedTaskEventNames.contains(eventName)

  /**
   * Determines if the provided event name should be collected.
   * @param eventName event name to check.
   * @return true if not excluded.
   */
  fun collectHistoryEvent(eventName: String): Boolean = !excludedHistoryEventNames.contains(eventName)
}

/**
 * Process variable properties.
 */
data class CamundaProcessVariableProperties(
  /**
   * Enabled by default.
   */
  val enabled: Boolean = true
)

/**
 * Task command enricher properties.
 */
data class TaskCollectorEnricherProperties(
  /**
   * Type of enricher, see TaskCollectorEnricherType values.
   */
  val type: TaskCollectorEnricherType = TaskCollectorEnricherType.processVariables,

  /** Process-variable payload filter configuration. */
  @NestedConfigurationProperty
  val processVariablesFilter: ProcessVariablesFilterProperties = ProcessVariablesFilterProperties(),

  /** Process-variable business-data correlation configuration. */
  @NestedConfigurationProperty
  val processVariablesCorrelator: ProcessVariablesCorrelatorProperties = ProcessVariablesCorrelatorProperties(),
)

/** Properties for the property-backed process-variable payload filter. */
data class ProcessVariablesFilterProperties(
  /** Enables creation of the property-backed filter. */
  val enabled: Boolean = false,
  /** Filters to apply, optionally scoped to a process definition or individual task definitions. */
  val filters: List<ProcessVariableFilterProperties> = emptyList()
) {
  /**
   * Converts the configured definitions into the variable filters used by the task enricher.
   *
   * @return process-level and task-level filters represented by these properties.
   */
  fun toVariableFilters(): Array<VariableFilter> = filters.flatMap { it.toVariableFilters() }.toTypedArray()
}

/** A single process-variable filter configured through application properties. */
data class ProcessVariableFilterProperties(
  /** Process definition key. Omit this for a global process-level filter. */
  val processDefinitionKey: String? = null,
  /** Whether the listed variables are included or excluded. */
  val filterType: FilterType = FilterType.EXCLUDE,
  /** Variables for a process-level filter. */
  val processVariables: List<String> = emptyList(),
  /** Variables per task definition for a task-level filter. */
  val taskVariables: Map<String, List<String>> = emptyMap()
) {
  /**
   * Converts this definition into its applicable process-level and task-level filters.
   *
   * @return a process-level filter when [processVariables] is configured, a task-level filter when
   * [taskVariables] is configured, or both when both property groups are present.
   * @throws IllegalArgumentException if task variables are configured without a process definition key.
   */
  fun toVariableFilters(): List<VariableFilter> = buildList {
    if (processVariables.isNotEmpty() || taskVariables.isEmpty()) {
      add(ProcessVariableFilter(processDefinitionKey, filterType, processVariables))
    }
    if (taskVariables.isNotEmpty()) {
      add(
        TaskVariableFilter(
          requireNotNull(processDefinitionKey) { "processDefinitionKey is required when taskVariables are configured" },
          filterType,
          taskVariables
        )
      )
    }
  }
}

/** Properties for the property-backed process-variable business-data correlator. */
data class ProcessVariablesCorrelatorProperties(
  /** Enables creation of the property-backed correlator. */
  val enabled: Boolean = false,
  /** Correlations, one entry for each process definition key. */
  val correlations: List<ProcessVariableCorrelation> = emptyList()
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
 * Properties controlling the transfer of process definitions deployments.
 */
data class CamundaProcessDefinitionCollectorProperties(

  /**
   * Disable by default.
   */
  val enabled: Boolean = false
)


/**
 * Properties controlling the transfer of process instance.
 */
data class CamundaProcessInstanceCollectorProperties(

  /**
   * Enabled by default.
   */
  val enabled: Boolean = true
)

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

/**
 * Configuration of the task importer.
 */
data class TaskImporterProperties(
  /**
   * Enables or disabled importer. Defaults to false.
   */
  val enabled: Boolean = false,

  /**
   * Configures the type of engine task command filter.
   * Defaults to `eventstore` allowing co-located deployed Taskpool Core to be used as a reference to filter commands.
   */
  val taskFilterType: EngineTaskCommandFilterType = EngineTaskCommandFilterType.eventstore
)

/**
 * Type
 */
enum class EngineTaskCommandFilterType {
  eventstore,
  custom
}
