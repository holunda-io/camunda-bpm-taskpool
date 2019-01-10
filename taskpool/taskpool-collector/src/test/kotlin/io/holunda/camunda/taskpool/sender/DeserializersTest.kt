package io.holunda.camunda.taskpool.sender

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.holunda.camunda.taskpool.api.task.ProcessReference
import io.holunda.camunda.taskpool.api.task.SourceReference
import io.holunda.camunda.taskpool.sender.accumulator.SourceReferenceDeserializer
import io.holunda.camunda.taskpool.sender.accumulator.VariableMapDeserializer
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables
import org.junit.Ignore
import org.junit.Test
import java.util.*

class DeserializersTest {

  @Test
  fun `serialize and deserialize source reference`() {

    val sourceReference: SourceReference = ProcessReference(
      instanceId = UUID.randomUUID().toString(),
      name = "name",
      applicationName = "application-name",
      definitionKey = "my-key",
      definitionId = "my-key:1",
      tenantId = "some",
      executionId = UUID.randomUUID().toString()
    )

    val mapper = jacksonObjectMapper().registerModule(
      SimpleModule().apply {
        addDeserializer(SourceReference::class.java, SourceReferenceDeserializer())
      }
    )
    val map: Map<String, Any> = jacksonObjectMapper().convertValue(sourceReference, object : TypeReference<Map<String, Any?>>() {})
    val converted = mapper.convertValue(map, ProcessReference::class.java)

    assertThat(converted).isEqualTo(sourceReference)
  }

  @Ignore
  @Test
  fun `serialize and deserialize variable map`() {

    val variables = Variables
      .createVariables()
      .putValue("simple", "value")
      .putValue("complex", DataStructure("some", 1))

    val mapper = jacksonObjectMapper().registerModule(
      SimpleModule().apply {
        addDeserializer(VariableMap::class.java, VariableMapDeserializer())
      }
    )
    val map: Map<String, Any> = jacksonObjectMapper().convertValue(variables, object : TypeReference<Map<String, Any?>>() {})
    val converted = mapper.convertValue(map, VariableMap::class.java)

    assertThat(converted).isEqualTo(variables)

  }
}

data class DataStructure(
  val value: String,
  val another: Int
)
