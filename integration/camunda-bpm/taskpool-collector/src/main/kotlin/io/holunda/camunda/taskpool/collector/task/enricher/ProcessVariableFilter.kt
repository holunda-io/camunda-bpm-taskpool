package io.holunda.camunda.taskpool.collector.task.enricher

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
