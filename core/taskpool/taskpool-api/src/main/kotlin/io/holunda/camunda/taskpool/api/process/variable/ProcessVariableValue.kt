package io.holunda.camunda.taskpool.api.process.variable

import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.value.TypedValue

/**
 * Carries the variable value, without dependency to concrete Java class.
 */
interface ProcessVariableValue {
  val type: ProcessVariableValueType
  val value: Any?
}

/**
 * Implementation of the process variable value, where the value itself is represented by Camunda's typed value.
 */
data class TypedValueProcessVariableValue(override val value: TypedValue)
  : ProcessVariableValue {
  override val type: ProcessVariableValueType = ProcessVariableValueType.TYPE_VALUE
}

/**
 * Implementation of the process variable value, where the value itself is represented by a 'primitive' class.
 */
data class PrimitiveProcessVariableValue(
  override val value: Any?
) : ProcessVariableValue {
  override val type: ProcessVariableValueType = ProcessVariableValueType.PRIMITIVE
}

/**
 * Implementation of the process variable value, where the value itself is represented by a map hierarchy of native classes.
 */
data class ObjectProcessVariableValue(
  override val value: VariableMap
) : ProcessVariableValue {
  override val type: ProcessVariableValueType = ProcessVariableValueType.OBJECT
}

/**
 * Type of process variable.
 */
enum class ProcessVariableValueType {
  /**
   * Un-serialized version referencing concrete classes not available in the framework.
   */
  TYPE_VALUE,
  /**
   * Some types are native to serialize. These are:
   * - Numbers (Integer, Float, Double)
   * - Boolean
   * - String
   * - Date
   */
  PRIMITIVE,
  /**
   * Everything else is an object.
   */
  OBJECT
}

