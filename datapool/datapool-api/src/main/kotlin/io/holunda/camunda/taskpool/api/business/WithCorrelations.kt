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

/**
 * Creates a new correlation map.
 */
fun newCorrelations(): CorrelationMap = Variables.createVariables()

/**
 * Adds correlation to current correlation map.
 */
fun VariableMap.addCorrelation(entryType: EntryType, entryId: EntryId) = this.putValueTyped(entryType, stringValue(entryId))!!

/**
 * Remove correlation from current correlation map.
 */
fun VariableMap.removeCorrelation(entryType: EntryType) = this.remove(entryType)

/**
 * Retrieve correlation for given entry type.
 */
fun VariableMap.getCorrelation(entryType: EntryType): EntryId = this.getValueTyped<StringValue>(entryType).value
