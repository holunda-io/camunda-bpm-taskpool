package io.holunda.polyflow.bus.jackson

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.holunda.camunda.taskpool.api.business.DataEntryState
import io.holunda.camunda.taskpool.api.business.ProcessingType
import io.holunda.camunda.taskpool.api.task.ProcessReference
import io.holunda.camunda.taskpool.api.task.SourceReference
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.util.*

class DeserializersTest {

  lateinit var mapper: ObjectMapper

  @BeforeEach
  fun initMapper() {
    mapper = jacksonObjectMapper()
      .configurePolyflowJacksonObjectMapper()
      .addMixIn(MyStructure::class.java, KotlinTypeInfo::class.java)
  }

  @Test
  fun `serialize and deserialize source reference`() {

    val sourceReference: SourceReference = sourceReference()

    // show written JSON
    println(mapper.writeValueAsString(sourceReference))

    val map: Map<String, Any> = mapper.convertValue(sourceReference, object : TypeReference<Map<String, Any>>() {})
    val converted = mapper.convertValue(map, ProcessReference::class.java)

    assertThat(converted).isEqualTo(sourceReference)
  }

  @Test
  fun `serialize and deserialize data entry state`() {

    val dataEntryState: DataEntryState = ProcessingType.IN_PROGRESS.of("test")

    // show written JSON
    println(mapper.writeValueAsString(dataEntryState))

    val map: Map<String, Any> = mapper.convertValue(dataEntryState, object : TypeReference<Map<String, Any>>() {})
    val converted = mapper.convertValue(map, DataEntryState::class.java)

    assertThat(converted).isEqualTo(dataEntryState)
  }

  @Test
  @Disabled("fails with: Could not resolve type id 'io.holunda.polyflow.bus.jackson.MyStructure' as a subtype of `java.util.Map<java.lang.String,java.lang.Object>`: Not a subtype at [Source: UNKNOWN; byte offset: #UNKNOWN]")
  fun `serialize and deserialize variable complex object`() {

    mapper.activateDefaultTypingAsProperty(mapper.polymorphicTypeValidator, ObjectMapper.DefaultTyping.NON_FINAL, "@class")
    val variables: VariableMap = Variables
      .createVariables()
      .putValue("simple", "value")
      .putValue("complex", DataStructure("some", 1))

    val original = MyStructure(
      name = "foo",
      variables = variables,
      source = sourceReference()
    )


    // show written JSON
    println(mapper.writeValueAsString(original))

    val variablesMap: Map<String, Any> = mapper.convertValue(variables, object : TypeReference<Map<String, Any>>() {})
    val variablesConverted = mapper.convertValue(variablesMap, VariableMap::class.java)
    assertThat(variablesConverted).isEqualTo(variablesMap)


    val objectMap: Map<String, Any> = mapper.convertValue(original, object : TypeReference<Map<String, Any>>() {})
    val objectConverted = mapper.convertValue(objectMap, MyStructure::class.java)
    assertThat(objectConverted).isEqualTo(original)


  }
}

fun sourceReference(): SourceReference = ProcessReference(
  instanceId = UUID.randomUUID().toString(),
  name = "name",
  applicationName = "application-name",
  definitionKey = "my-key",
  definitionId = "my-key:1",
  tenantId = "some",
  executionId = UUID.randomUUID().toString()
)


data class DataStructure(
  val value: String,
  val another: Int
)

data class MyStructure(
  val name: String,
  val variables: VariableMap,
  val source: SourceReference
)
