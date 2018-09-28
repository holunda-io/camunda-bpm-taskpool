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
    val taskCorrelations = processCorrelations.correlations[taskDefinitionKey] ?: return result

    taskCorrelations.entries.forEach {

      // get string representation of the variable, if found and store it under the entry type
      if (variables.containsKey(it.key)) {
        result.addCorrelation(it.value, variables.getValue(it.key).toString())
      }
    }
    return result
  }
}

data class ProcessVariableCorrelation(
  val processDefinitionKey: ProcessDefinitionKey,
  val correlations: Map<TaskDefinitionKey, Map<String, EntryType>>
)

