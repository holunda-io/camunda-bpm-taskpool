package io.holunda.polyflow.taskpool

import com.fasterxml.jackson.databind.ObjectMapper
import io.holunda.camunda.taskpool.api.process.variable.ObjectProcessVariableValue
import io.holunda.camunda.taskpool.api.process.variable.PrimitiveProcessVariableValue
import io.holunda.camunda.taskpool.api.process.variable.ProcessVariableValue
import io.holunda.camunda.taskpool.api.process.variable.TypedValueProcessVariableValue
import io.holunda.camunda.variable.serializer.serialize

/**
 * Serialize process variable.
 */
fun ProcessVariableValue.serialize(objectMapper: ObjectMapper): ProcessVariableValue =
  if (this is TypedValueProcessVariableValue) {
    if (this.value.type.isPrimitiveValueType) {
      PrimitiveProcessVariableValue(this.value.value)
    } else {
      ObjectProcessVariableValue(serialize(this.value.value, objectMapper))
    }
  } else {
    this
  }

