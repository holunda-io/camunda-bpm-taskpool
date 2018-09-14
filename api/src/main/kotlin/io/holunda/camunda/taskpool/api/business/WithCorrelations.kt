package io.holunda.camunda.taskpool.api.business

import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables
import org.camunda.bpm.engine.variable.Variables.stringValue
import org.camunda.bpm.engine.variable.value.StringValue

/**
 * Correlations are represented as a variable map with a key set to {@link DataIdentity#entryType}
 * and the value to {@link DataIdentity#entryId}.
 */
interface WithCorrelations {
  val correlations: CorrelationMap
}

typealias CorrelationMap = VariableMap

fun newCorrelations(): VariableMap = Variables.createVariables()
fun VariableMap.addCorrelation(entryType: EntryType, entryId: EntryId) = putValueTyped(entryType, stringValue(entryId))
fun VariableMap.removeCorrelation(entryType: EntryType) = remove(entryType)
fun VariableMap.getCorrelation(entryType: EntryType): EntryId = getValueTyped<StringValue>(entryType).value
