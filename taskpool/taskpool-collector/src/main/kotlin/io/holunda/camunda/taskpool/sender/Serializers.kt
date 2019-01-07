package io.holunda.camunda.taskpool.sender

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import io.holunda.camunda.taskpool.api.task.ProcessReference
import io.holunda.camunda.taskpool.api.task.SourceReference
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables

open class VariableMapDeserializer : StdDeserializer<VariableMap>(VariableMap::class.java) {
  override fun deserialize(parser: JsonParser?, context: DeserializationContext?): VariableMap {
    return Variables.createVariables()
  }
}

open class SourceReferenceDeserializer : StdDeserializer<SourceReference>(VariableMap::class.java) {
  override fun deserialize(parser: JsonParser?, context: DeserializationContext?): SourceReference {
    return ProcessReference("i", "e", "d", "dk", "n", "a")
  }
}
