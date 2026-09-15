package io.holunda.polyflow.taskpool.collector.task.enricher

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

typealias ProcessDefinitionKey = String
typealias TaskDefinitionKey = String
typealias VariableName = String

/**
 * Filter type.
 */
enum class FilterType {
  /**
   * Include filter.
   */
  INCLUDE,

  /**
   * Exclude filter.
   */
  EXCLUDE
}

