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
   * Filters variables for a task using the filters applicable to its process definition.
   *
   * @param processDefinitionKey key of the process definition that owns the task.
   * @param taskDefinitionKey key of the task definition receiving the variables.
   * @param variables variables available from the process engine.
   * @return variables that pass every applicable filter.
   */
  fun filterVariables(processDefinitionKey: ProcessDefinitionKey, taskDefinitionKey: TaskDefinitionKey, variables: VariableMap): VariableMap {
    val variableFilters = filtersFor(processDefinitionKey)
    if (variableFilters.isEmpty()) return variables
    return variables.filterKeys { variableName -> variableFilters.all { it.filter(taskDefinitionKey, variableName) } }
  }

  /**
   * Checks whether a variable is included by the filters applicable to a process definition.
   *
   * @param processDefinitionKey key of the process definition to evaluate.
   * @param variableName name of the variable to evaluate.
   * @return `true` when at least one applicable filter exists and every filter includes the variable.
   */
  fun isIncluded(processDefinitionKey: ProcessDefinitionKey, variableName: VariableName): Boolean {
    val variableFilters = filtersFor(processDefinitionKey)
    return variableFilters.isNotEmpty() && variableFilters.all { it.filter("__not_relevant", variableName) }
  }

  private fun filtersFor(processDefinitionKey: ProcessDefinitionKey): List<VariableFilter> =
    processSpecificFilters[processDefinitionKey] ?: commonFilters
}
