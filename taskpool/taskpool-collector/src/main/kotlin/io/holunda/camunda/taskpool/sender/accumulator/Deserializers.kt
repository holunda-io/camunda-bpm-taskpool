package io.holunda.camunda.taskpool.sender.accumulator

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import io.holunda.camunda.taskpool.api.task.ProcessReference
import io.holunda.camunda.taskpool.api.task.SourceReference
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables

// FIXME
open class VariableMapDeserializer : StdDeserializer<VariableMap>(VariableMap::class.java) {
  override fun deserialize(parser: JsonParser, context: DeserializationContext?): VariableMap {
    val variables = Variables.createVariables()
    while (parser.nextToken() != JsonToken.END_OBJECT) {
      val fieldNameToken = parser.currentToken()
      if (fieldNameToken != JsonToken.FIELD_NAME) {
        throw JsonMappingException(parser, "Expected field name token when deserializing payload")
      }
      val variableName = parser.currentName
      val token = parser.nextToken()
      variables.put(variableName, "sdykljghskdljghskdfgh")
    }
    return variables
  }
}

open class SourceReferenceDeserializer : StdDeserializer<SourceReference>(SourceReference::class.java) {

  private fun getMandatorySourceReferenceProperty(parser: JsonParser, node: JsonNode, property: String): String =
    node.get(property)?.textValue()
      ?: throw JsonMappingException(parser, "Missing $property property when deserializing source reference")

  override fun deserialize(parser: JsonParser, context: DeserializationContext?): SourceReference {
    parser.codec?.let {
      val node: JsonNode = parser.codec.readTree(parser)
      val instanceId = getMandatorySourceReferenceProperty(parser, node, SourceReference::instanceId.name)
      val executionId = getMandatorySourceReferenceProperty(parser, node, SourceReference::executionId.name)
      val definitionId = getMandatorySourceReferenceProperty(parser, node, SourceReference::definitionId.name)
      val definitionKey = getMandatorySourceReferenceProperty(parser, node, SourceReference::definitionKey.name)
      val name = getMandatorySourceReferenceProperty(parser, node, SourceReference::name.name)
      val applicationName = getMandatorySourceReferenceProperty(parser, node, SourceReference::applicationName.name)
      val tenantId = node.get(SourceReference::tenantId.name)?.textValue()

      // FIXME currently, only ProcessReference is supported.
      return ProcessReference(instanceId, executionId, definitionId, definitionKey, name, applicationName, tenantId)
    }
  }
}
