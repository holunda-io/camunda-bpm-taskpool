package io.holunda.camunda.taskpool.enricher

import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables

class ProcessVariablesFilter(vararg filters: ProcessVariableFilter) {

  private val filter: Map<ProcessDefinitionKey, ProcessVariableFilter> = filters.associate { it.processDefinitionKey to it }

  fun filterVariables(processDefinitionKey: ProcessDefinitionKey, taskDefinitionKey: TaskDefinitionKey, variables: VariableMap): VariableMap {

    val processFilter = filter[processDefinitionKey] ?: return variables
    val taskFilter = processFilter.variableFilter[taskDefinitionKey] ?: return variables

    return variables.filterKeys { (processFilter.filterType == FilterType.INCLUDE) == taskFilter.contains(it) }
  }
}

data class ProcessVariableFilter(
  val processDefinitionKey: ProcessDefinitionKey,
  val filterType: FilterType,
  val variableFilter: Map<TaskDefinitionKey, List<VariableName>>
)

typealias ProcessDefinitionKey = String
typealias TaskDefinitionKey = String
typealias VariableName = String

enum class FilterType {
  INCLUDE,
  EXCLUDE
}

