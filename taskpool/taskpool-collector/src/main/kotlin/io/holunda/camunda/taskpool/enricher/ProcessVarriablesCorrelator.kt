package io.holunda.camunda.taskpool.enricher

import io.holunda.camunda.taskpool.api.business.CorrelationMap
import io.holunda.camunda.taskpool.api.business.EntryType
import io.holunda.camunda.taskpool.api.business.addCorrelation
import io.holunda.camunda.taskpool.api.business.newCorrelations
import org.camunda.bpm.engine.variable.VariableMap

class ProcessVariablesCorrelator(vararg correlations: ProcessVariableCorrelation) {

  private val all: Map<ProcessDefinitionKey, ProcessVariableCorrelation> = correlations.associate { it.processDefinitionKey to it }

  fun correlateVariables(processDefinitionKey: ProcessDefinitionKey, taskDefinitionKey: TaskDefinitionKey, variables: VariableMap): CorrelationMap {

    val result = newCorrelations()
    val processCorrelations: ProcessVariableCorrelation = all[processDefinitionKey] ?: return result

    // handle global correlations
    processCorrelations.globalCorrelations.forEach{
      // get string representation of the variable, if found and store it under the entry type
      if (variables.containsKey(it.key)) {
        result.addCorrelation(it.value, variables.getValue(it.key).toString())
      }
    }

    // handle task correlations
    val taskCorrelations = processCorrelations.correlations[taskDefinitionKey] ?: emptyMap()
    taskCorrelations.entries.forEach {

      // get string representation of the variable, if found and store it under the entry type
      if (variables.containsKey(it.key)) {
        result.addCorrelation(it.value, variables.getValue(it.key).toString())
      }
    }
    return result
  }
}

/**
 * Describes correlation between data entries for a business process definition.
 * @param processDefinitionKey process definition key.
 * @param correlations a map from task definition key to correlation map (variableName, entryType).
 * @param globalCorrelations a global (per-process) correlation map (variableName, entryType).
 */
class ProcessVariableCorrelation(
  val processDefinitionKey: ProcessDefinitionKey,
  val correlations: Map<TaskDefinitionKey, Map<String, EntryType>>,
  val globalCorrelations: Map<String, EntryType> = emptyMap()
)

