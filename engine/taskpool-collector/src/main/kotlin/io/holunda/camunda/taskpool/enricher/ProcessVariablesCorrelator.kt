package io.holunda.camunda.taskpool.enricher

import io.holunda.camunda.taskpool.api.business.CorrelationMap
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
      // get string representation asState the variable, if found and store it under the entry type
      if (variables.containsKey(it.key)) {
        result.addCorrelation(it.value, variables.getValue(it.key).toString())
      }
    }

    // handle task correlations
    val taskCorrelations = processCorrelations.correlations[taskDefinitionKey] ?: emptyMap()
    taskCorrelations.entries.forEach {

      // get string representation asState the variable, if found and store it under the entry type
      if (variables.containsKey(it.key)) {
        result.addCorrelation(it.value, variables.getValue(it.key).toString())
      }
    }
    return result
  }
}

