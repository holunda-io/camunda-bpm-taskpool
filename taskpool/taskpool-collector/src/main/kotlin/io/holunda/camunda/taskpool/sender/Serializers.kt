package io.holunda.camunda.taskpool.sender

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

open class VariableMapDeserializer : StdDeserializer<VariableMap>(VariableMap::class.java) {
  override fun deserialize(parser: JsonParser?, context: DeserializationContext?): VariableMap {
    while (parser?.nextToken() != JsonToken.END_OBJECT) {
      val fieldNameToken = parser?.currentToken()
      if (fieldNameToken != JsonToken.FIELD_NAME) {
        throw JsonMappingException(parser, "Expected field name token when deserializing payload")
      }
      val variableName = parser.currentName
      val token = parser.nextToken()
    }
    return Variables.createVariables()
  }
}

open class SourceReferenceDeserializer : StdDeserializer<SourceReference>(VariableMap::class.java) {

  private fun getMandatorySourceReferenceProperty(parser: JsonParser, node: JsonNode, property: String): String =
    node.get(property)?.textValue() ?: throw JsonMappingException(parser, "Missing $property property when deserializing source reference")

  override fun deserialize(parser: JsonParser?, context: DeserializationContext?): SourceReference {
    parser?.codec?.let {
      val node : JsonNode = parser.codec.readTree(parser)
      val instanceId = getMandatorySourceReferenceProperty(parser, node, "instanceId")
      val executionId = getMandatorySourceReferenceProperty(parser, node, "executionId")
      val definitionId = getMandatorySourceReferenceProperty(parser, node, "definitionId")
      val definitionKey = getMandatorySourceReferenceProperty(parser, node, "definitionKey")
      val name = getMandatorySourceReferenceProperty(parser, node, "name")
      val applicationName = getMandatorySourceReferenceProperty(parser, node, "applicationName")
      val tenantId = node.get("tenantId")?.textValue()
      return ProcessReference(instanceId, executionId, definitionId, definitionKey, name, applicationName, tenantId)
    }
  }
}
