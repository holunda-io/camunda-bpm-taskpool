package io.holunda.camunda.variable.serializer

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables

/**
 * Serialize variables into a map using provided object mapper.
 * @param payload serialization content
 * @param mapper optional mapper, if not provided, Jackson default mapper will be used.
 */
fun serialize(payload: Any, mapper: ObjectMapper): VariableMap {
  return Variables.createVariables().apply {
    this.putAll(mapper.convertValue(payload, object : TypeReference<Map<String, Any>>() {}))
  }
}
