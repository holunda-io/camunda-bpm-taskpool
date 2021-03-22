package io.holunda.camunda.taskpool.collector.task.enricher

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

  fun filterVariables(processDefinitionKey: ProcessDefinitionKey, taskDefinitionKey: TaskDefinitionKey, variables: VariableMap): VariableMap {
    val variableFilter = processSpecificFilters[processDefinitionKey] ?: commonFilter ?: return variables
    return variables.filterKeys { variableFilter.filter(taskDefinitionKey, it) }
  }
}

/**
 * This filter allows to either explicitly include (whitelist) or exclude (blacklist) process variables for all user tasks of a certain
 * process (if a process definition key is given), or for all user tasks of <i>all</i> processes (if no process definition key is given).
 * If a differentiation between individual user tasks of a process is required, use a {@link TaskVariableFilter} instead.
 */
data class ProcessVariableFilter(
  override val processDefinitionKey: ProcessDefinitionKey?,
  val filterType: FilterType,
  val processVariables: List<VariableName> = emptyList()
): VariableFilter {

  constructor(filterType: FilterType, processVariables: List<VariableName>): this(null, filterType, processVariables)

  override fun filter(taskDefinitionKey: TaskDefinitionKey, variableName: VariableName): Boolean {
    return (filterType == FilterType.INCLUDE) == processVariables.contains(variableName)
  }

}

/**
 * This filter allows to either explicitly include (whitelist) or exclude (blacklist) process variables for user tasks of a certain process.
 * If the differentiation between individual user tasks is not required, use a {@link ProcessVariableFilter} instead.
 */
data class TaskVariableFilter(
  override val processDefinitionKey: ProcessDefinitionKey,
  val filterType: FilterType,
  val taskVariables: Map<TaskDefinitionKey, List<VariableName>> = emptyMap()
): VariableFilter {

  override fun filter(taskDefinitionKey: TaskDefinitionKey, variableName: VariableName): Boolean {
    val taskFilter = taskVariables[taskDefinitionKey] ?: return true
    return (filterType == FilterType.INCLUDE) == taskFilter.contains(variableName)
  }

}

/**
 * To be implemented by classes that filter process variables. Used during enrichment to decide which process variables are added to a task's payload.
 */
interface VariableFilter {

  val processDefinitionKey: ProcessDefinitionKey?

  /**
   * Returns whether or not the process variable with the given name shall be contained in the payload of the given task.
   * @param taskDefinitionKey the key of the task to be enriched
   * @param variableName the name of the process variable
   */
  fun filter(taskDefinitionKey: TaskDefinitionKey, variableName: VariableName): Boolean

}

typealias ProcessDefinitionKey = String
typealias TaskDefinitionKey = String
typealias VariableName = String

enum class FilterType {
  INCLUDE,
  EXCLUDE
}

