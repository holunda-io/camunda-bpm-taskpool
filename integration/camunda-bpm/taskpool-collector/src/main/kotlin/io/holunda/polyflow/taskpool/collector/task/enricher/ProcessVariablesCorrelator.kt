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
   * Correlates process-level variables for a process-instance lifecycle event.
   *
   * Only global correlations apply because lifecycle events are not associated with a task definition.
   */
  open fun correlateVariables(processDefinitionKey: ProcessDefinitionKey, variables: VariableMap): CorrelationMap {
    val result = newCorrelations()
    val processCorrelations = all[processDefinitionKey] ?: return result
    processCorrelations.globalCorrelations.forEach {
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

  /**
   * Returns process-variable names required by global correlations for a lifecycle event.
   *
   * @param processDefinitionKey key of the process definition that owns the instance.
   * @return names of the process variables referenced by global correlation definitions.
   */
  fun variableNamesFor(processDefinitionKey: ProcessDefinitionKey): Set<VariableName> =
    all[processDefinitionKey]?.globalCorrelations?.mapTo(mutableSetOf()) { it.entryIdVariableName } ?: emptySet()
}
