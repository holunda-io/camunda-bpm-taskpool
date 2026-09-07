package io.holunda.polyflow.taskpool.collector

import io.holunda.polyflow.taskpool.collector.task.enricher.FilterType
import io.holunda.polyflow.taskpool.collector.task.enricher.ProcessVariableFilter
import io.holunda.polyflow.taskpool.collector.task.enricher.TaskVariableFilter
import io.holunda.polyflow.taskpool.collector.task.enricher.VariableFilter
import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Properties for the property-backed [io.holunda.polyflow.taskpool.collector.task.enricher.ProcessVariablesFilter].
 */
@ConfigurationProperties("polyflow.integration.collector.camunda.process-variables-filter")
data class ProcessVariablesFilterProperties(
  /** Enables creation of the property-backed filter. */
  val enabled: Boolean = false,
  /** Filters to apply, optionally scoped to a process definition or individual task definitions. */
  val filters: List<ProcessVariableFilterProperties> = emptyList()
) {
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
