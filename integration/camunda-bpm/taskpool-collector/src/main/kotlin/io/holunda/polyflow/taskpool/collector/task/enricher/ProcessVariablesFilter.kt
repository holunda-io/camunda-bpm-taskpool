package io.holunda.polyflow.taskpool.collector.task.enricher

import io.holunda.polyflow.taskpool.filterKeys
import org.camunda.bpm.engine.variable.VariableMap

/**
 * Groups one or more {@linkplain VariableFilter process variable filters}. Assumes (but does not enforce) that among the given individual filter instances,
 * at most one is contained for any specific process, and at most one "global" filter (that is applied to all processes) is contained.
 */
class ProcessVariablesFilter(
  vararg variableFilters: VariableFilter
) {

  private var processSpecificFilters: Map<ProcessDefinitionKey, VariableFilter> =
    variableFilters.filter { it.processDefinitionKey != null }.associateBy { it.processDefinitionKey!! }
  private var commonFilter: VariableFilter? = variableFilters.find { it.processDefinitionKey == null }

  /**
   * Filters the list of variables.
   * @return variables that have not been filtered out by the filters.
   */
  fun filterVariables(processDefinitionKey: ProcessDefinitionKey, taskDefinitionKey: TaskDefinitionKey, variables: VariableMap): VariableMap {
    val variableFilter = processSpecificFilters[processDefinitionKey] ?: commonFilter ?: return variables
    return variables.filterKeys { variableFilter.filter(taskDefinitionKey, it) }
  }

  /**
   * Checks whether a variable is passing the variable filter or not.
   * @return true, if the variable is passing the filter.
   */
  fun isIncluded(processDefinitionKey: ProcessDefinitionKey, variableName: VariableName): Boolean {
    val variableFilter = processSpecificFilters[processDefinitionKey] ?: commonFilter ?: return false
    return variableFilter.filter("__not_relevant", variableName)
  }
}
