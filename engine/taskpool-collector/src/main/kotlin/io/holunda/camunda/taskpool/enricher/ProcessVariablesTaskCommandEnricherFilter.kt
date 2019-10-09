package io.holunda.camunda.taskpool.enricher

import org.camunda.bpm.engine.variable.VariableMap

class ProcessVariablesFilter(
  vararg variableFilters: VariableFilter
) {

  private var processSpecificFilters: Map<ProcessDefinitionKey, VariableFilter> =
    variableFilters.filter { it.processDefinitionKey != null }.associateBy { it.processDefinitionKey!! }
  private var commonFilter: VariableFilter? = variableFilters.find { it.processDefinitionKey == null }

  fun filterVariables(processDefinitionKey: ProcessDefinitionKey, taskDefinitionKey: TaskDefinitionKey, variables: VariableMap): VariableMap {
    val variableFilter = processSpecificFilters[processDefinitionKey] ?: commonFilter ?: return variables
    return variables.filterKeys { variableFilter.filter(processDefinitionKey, taskDefinitionKey, it) }
  }
}

data class ProcessVariableFilter(
  override val processDefinitionKey: ProcessDefinitionKey?,
  val filterType: FilterType,
  val processVariables: List<VariableName> = emptyList()
): VariableFilter {

  constructor(filterType: FilterType, processVariables: List<VariableName>): this(null, filterType, processVariables)

  override fun filter(processDefinitionKey: ProcessDefinitionKey, taskDefinitionKey: TaskDefinitionKey, variableName: VariableName): Boolean {
    // this filter applies if it has either no process definition key, or the same process definition key as that process to which the given variable belongs
    return if (this.processDefinitionKey != null && processDefinitionKey != this.processDefinitionKey) true
      else (filterType == FilterType.INCLUDE) == processVariables.contains(variableName)
  }

}

data class TaskVariableFilter(
  override val processDefinitionKey: ProcessDefinitionKey,
  val filterType: FilterType,
  val taskVariables: Map<TaskDefinitionKey, List<VariableName>> = emptyMap()
): VariableFilter {

  override fun filter(processDefinitionKey: ProcessDefinitionKey, taskDefinitionKey: TaskDefinitionKey, variableName: VariableName): Boolean {
    if (processDefinitionKey != this.processDefinitionKey) {
      return true
    }
    val taskFilter = taskVariables[taskDefinitionKey] ?: return true
    return (filterType == FilterType.INCLUDE) == taskFilter.contains(variableName)
  }

}

interface VariableFilter {

  val processDefinitionKey: ProcessDefinitionKey?

  fun filter(processDefinitionKey: ProcessDefinitionKey, taskDefinitionKey: TaskDefinitionKey, variableName: VariableName): Boolean

}

typealias ProcessDefinitionKey = String
typealias TaskDefinitionKey = String
typealias VariableName = String

enum class FilterType {
  INCLUDE,
  EXCLUDE
}

