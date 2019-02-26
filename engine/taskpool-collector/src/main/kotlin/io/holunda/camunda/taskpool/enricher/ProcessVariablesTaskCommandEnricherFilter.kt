package io.holunda.camunda.taskpool.enricher

import org.camunda.bpm.engine.variable.VariableMap

class ProcessVariablesFilter(
  vararg filters: ProcessVariableFilter,
  private val filter: Map<ProcessDefinitionKey, ProcessVariableFilter> = filters.associate { it.processDefinitionKey to it }
) {

  fun filterVariables(processDefinitionKey: ProcessDefinitionKey, taskDefinitionKey: TaskDefinitionKey, variables: VariableMap): VariableMap {

    val processFilter = filter[processDefinitionKey] ?: return variables
    return when (processFilter.filterType) {
      FilterType.INCLUDE, FilterType.EXCLUDE -> {
        val taskFilter = processFilter.taskVariableFilter[taskDefinitionKey] ?: return variables
        variables.filterKeys { (processFilter.filterType == FilterType.INCLUDE) == taskFilter.contains(it) }
      }
      FilterType.PROCESS_EXCLUDE, FilterType.PROCESS_INCLUDE -> {
        variables.filterKeys { (processFilter.filterType == FilterType.PROCESS_INCLUDE) == processFilter.globalVariableFilter.contains(it) }
      }
    }
  }
}

data class ProcessVariableFilter(
  val processDefinitionKey: ProcessDefinitionKey,
  val filterType: FilterType,
  val taskVariableFilter: Map<TaskDefinitionKey, List<VariableName>> = emptyMap(),
  val globalVariableFilter: List<VariableName> = emptyList()
)

typealias ProcessDefinitionKey = String
typealias TaskDefinitionKey = String
typealias VariableName = String

enum class FilterType {
  INCLUDE,
  EXCLUDE,
  PROCESS_INCLUDE,
  PROCESS_EXCLUDE
}

