package io.holunda.camunda.taskpool.sender

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.holunda.camunda.taskpool.KotlinTypeInfo
import io.holunda.camunda.taskpool.api.task.ProcessReference
import io.holunda.camunda.taskpool.api.task.SourceReference
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables
import org.camunda.bpm.engine.variable.impl.VariableMapImpl
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import java.util.*

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@valueType", include = JsonTypeInfo.As.PROPERTY)
class VariableMapKotlinTypeInfo


class DeserializersTest {


  lateinit var mapper: ObjectMapper

  @Before
  fun initMapper() {
    mapper = jacksonObjectMapper()
      .addMixIn(MyStructure::class.java, KotlinTypeInfo::class.java)
      .addMixIn(VariableMap::class.java, KotlinTypeInfo::class.java)
      .addMixIn(SourceReference::class.java, KotlinTypeInfo::class.java)
      .apply {
        registerSubtypes(VariableMapImpl::class.java, VariableMapImpl::class.java)
      }
      .registerModule(SimpleModule()
        .apply {
          addAbstractTypeMapping(VariableMap::class.java, VariableMapImpl::class.java)
          // addAbstractTypeMapping(SourceReference::class.java, ProcessReference::class.java)
          // addAbstractTypeMapping(SourceReference::class.java, CaseReference::class.java)
        })
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

  @Ignore
  @Test
  fun `serialize and deserialize variable complex object`() {

    mapper.enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.NON_FINAL, "@class")
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
