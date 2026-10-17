package io.holunda.polyflow.taskpool

import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables

/**
 * Add all elements from one variable map to another.
 * @param source source of the variables.
 */
fun VariableMap.putAllTyped(source: VariableMap) {
  source.keys.forEach {
    this.putValueTyped(it, source.getValueTyped(it))
  }
}

/**
 * Filters keys by a predicate.
 * @param predicate filter function.
 * @return new variable map containing only entries for which the keys are matching the filter function.
 */
inline fun VariableMap.filterKeys(predicate: (String) -> Boolean): VariableMap {
  val result = Variables.createVariables()
  for (entry in this) {
    if (predicate(entry.key)) {
      result[entry.key] = entry.value
    }
  }
  return result
}
