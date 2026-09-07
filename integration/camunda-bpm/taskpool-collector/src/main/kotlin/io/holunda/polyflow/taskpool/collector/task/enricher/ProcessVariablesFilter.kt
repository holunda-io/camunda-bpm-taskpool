package io.holunda.polyflow.taskpool.collector.task.enricher

import io.holunda.polyflow.taskpool.filterKeys
import org.camunda.bpm.engine.variable.VariableMap

/**
 * Groups one or more {@linkplain VariableFilter process variable filters}. Filters scoped to the same process definition are
 * combined, so a variable must be included by every matching filter. Process-specific filters take precedence over global filters.
 */
open class ProcessVariablesFilter(
  vararg variableFilters: VariableFilter
) {

  private val processSpecificFilters: Map<ProcessDefinitionKey, List<VariableFilter>> =
    variableFilters.filter { it.processDefinitionKey != null }.groupBy { it.processDefinitionKey!! }
  private val commonFilters: List<VariableFilter> = variableFilters.filter { it.processDefinitionKey == null }

  /**
   * Filters the list of variables.
   * @return variables that have not been filtered out by the filters.
   */
  fun filterVariables(processDefinitionKey: ProcessDefinitionKey, taskDefinitionKey: TaskDefinitionKey, variables: VariableMap): VariableMap {
    val variableFilters = filtersFor(processDefinitionKey)
    if (variableFilters.isEmpty()) return variables
    return variables.filterKeys { variableName -> variableFilters.all { it.filter(taskDefinitionKey, variableName) } }
  }

  /**
   * Checks whether a variable is passing the variable filter or not.
   * @return true, if the variable is passing the filter.
   */
  fun isIncluded(processDefinitionKey: ProcessDefinitionKey, variableName: VariableName): Boolean {
    val variableFilters = filtersFor(processDefinitionKey)
    return variableFilters.isNotEmpty() && variableFilters.all { it.filter("__not_relevant", variableName) }
  }

  private fun filtersFor(processDefinitionKey: ProcessDefinitionKey): List<VariableFilter> =
    processSpecificFilters[processDefinitionKey] ?: commonFilters
}
