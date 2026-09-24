package io.holunda.polyflow.taskpool.collector.task.enricher

import io.holunda.camunda.taskpool.api.business.CorrelationMap
import io.holunda.camunda.taskpool.api.business.addCorrelation
import io.holunda.camunda.taskpool.api.business.newCorrelations
import org.camunda.bpm.engine.variable.VariableMap

/**
 * Correlator for process variables.
 */
open class ProcessVariablesCorrelator(vararg correlations: ProcessVariableCorrelation) {

  private val all: Map<ProcessDefinitionKey, ProcessVariableCorrelation> = correlations.associate { it.processDefinitionKey to it }

  /**
   * Correlates variables from a given correlation map for a provided process definition and task definition.
   */
  open fun correlateVariables(processDefinitionKey: ProcessDefinitionKey, taskDefinitionKey: TaskDefinitionKey, variables: VariableMap): CorrelationMap {

    val result = newCorrelations()
    val processCorrelations: ProcessVariableCorrelation = all[processDefinitionKey] ?: return result

    // handle global correlations
    processCorrelations.globalCorrelations.forEach{
      // get string representation of the variable, if found and store it under the entry type
      if (variables.containsKey(it.entryIdVariableName)) {
        result.addCorrelation(it.entryType, variables.getValue(it.entryIdVariableName).toString())
      }
    }

    // handle task correlations
    val taskCorrelations = processCorrelations.correlations[taskDefinitionKey] ?: emptyList()
    taskCorrelations.forEach {

      // get string representation of the variable, if found and store it under the entry type
      if (variables.containsKey(it.entryIdVariableName)) {
        result.addCorrelation(it.entryType, variables.getValue(it.entryIdVariableName).toString())
      }
    }
    return result
  }

  /**
   * Returns variable names required to create correlations for a task.
   *
   * @param processDefinitionKey key of the process definition that owns the task.
   * @param taskDefinitionKey key of the task definition receiving the correlations.
   * @return names of the referenced process variables.
   */
  fun variableNamesFor(processDefinitionKey: ProcessDefinitionKey, taskDefinitionKey: TaskDefinitionKey): Set<VariableName> {
    val processCorrelations = all[processDefinitionKey] ?: return emptySet()
    return processCorrelations.globalCorrelations
      .plus(processCorrelations.correlations[taskDefinitionKey].orEmpty())
      .mapTo(mutableSetOf()) { it.entryIdVariableName }
  }
}
