package io.holunda.camunda.taskpool.enricher

import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables

fun VariableMap.putAllTyped(source: VariableMap) {
  source.keys.forEach {
    this.putValueTyped(it, source.getValueTyped(it))
  }
}

inline fun VariableMap.filterKeys(predicate: (String) -> Boolean): VariableMap {
  val result = Variables.createVariables()
  for (entry in this) {
    if (predicate(entry.key)) {
      result[entry.key] = entry.value
    }
  }
  return result
}
