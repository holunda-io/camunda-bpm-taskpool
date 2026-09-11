package io.holunda.polyflow.bus.jackson

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.holunda.camunda.taskpool.api.business.*
import io.holunda.camunda.taskpool.api.task.ProcessReference
import io.holunda.camunda.taskpool.api.task.SourceReference
import io.holunda.polyflow.view.DataEntry
import io.holunda.polyflow.view.Task
import io.holunda.polyflow.view.auth.User
import io.holunda.polyflow.view.query.data.DataEntriesForUserQuery
import io.holunda.polyflow.view.query.data.DataEntriesQuery
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
    mapper = jacksonObjectMapper().configurePolyflowJacksonObjectMapper().addMixIn(MyStructure::class.java, KotlinTypeInfo::class.java)
  }

  @Test
  fun `serialize and deserialize source reference`() {

    val sourceReference: SourceReference = sourceReference()

    val map: Map<String, Any> = mapper.convertValue(sourceReference, object : TypeReference<Map<String, Any>>() {})
    val converted = mapper.convertValue(map, ProcessReference::class.java)

    assertThat(converted).isEqualTo(sourceReference)
  }

  @Test
  fun `serialize and deserialize data entry state`() {

    val dataEntryState: DataEntryState = ProcessingType.IN_PROGRESS.of("test")

    val map: Map<String, Any> = mapper.convertValue(dataEntryState, object : TypeReference<Map<String, Any>>() {})
    val converted = mapper.convertValue(map, DataEntryState::class.java)

    assertThat(converted).isEqualTo(dataEntryState)
  }

  @Test
  @Disabled("fails with: Could not resolve type id 'io.holunda.polyflow.bus.jackson.MyStructure' as a subtype of `java.util.Map<java.lang.String,java.lang.Object>`: Not a subtype at [Source: UNKNOWN; byte offset: #UNKNOWN]")
  fun `serialize and deserialize variable complex object`() {

    mapper.activateDefaultTypingAsProperty(mapper.polymorphicTypeValidator, ObjectMapper.DefaultTyping.NON_FINAL, "@class")
    val variables: VariableMap = Variables.createVariables().putValue("simple", "value").putValue("complex", DataStructure("some", 1))

    val original = MyStructure(
      name = "foo", variables = variables, source = sourceReference()
    )

    val variablesMap: Map<String, Any> = mapper.convertValue(variables, object : TypeReference<Map<String, Any>>() {})
    val variablesConverted = mapper.convertValue(variablesMap, VariableMap::class.java)
    assertThat(variablesConverted).isEqualTo(variablesMap)

    val objectMap: Map<String, Any> = mapper.convertValue(original, object : TypeReference<Map<String, Any>>() {})
    val objectConverted = mapper.convertValue(objectMap, MyStructure::class.java)
    assertThat(objectConverted).isEqualTo(original)
  }

  // see https://github.com/holunda-io/camunda-bpm-taskpool/issues/609
  @Test
  fun `can serialize and deserialize datEntry with jackson`() {
    val dataEntry = DataEntry(entryType = "A", entryId = "1", payload = Variables.putValue("x", "y"), name = "A1", type = "A", applicationName = "y")

    val json = mapper.writeValueAsString(dataEntry)

    val deserialized = mapper.readValue<DataEntry>(json)

    assertThat(deserialized).isEqualTo(dataEntry)
  }

  // see https://github.com/holunda-io/camunda-bpm-taskpool/issues/609
  @Test
  fun `can serialize and deserialize task with jackson`() {
    fun correlationMap(vararg dataEntries: DataEntry): CorrelationMap {
      val correlations = newCorrelations()

      dataEntries.forEach { correlations.addCorrelation(it.entryType, it.entryId) }

      return correlations
    }

    val dataEntry1 = DataEntry(entryType = "A", entryId = "1", payload = Variables.putValue("x", "y"), name = "A1", type = "A", applicationName = "y")
    val task = Task(
      id = UUID.randomUUID().toString(), sourceReference = ProcessReference(
        instanceId = UUID.randomUUID().toString(),
        applicationName = "test application",
        definitionId = "myProcess:1",
        definitionKey = "myProcess",
        executionId = UUID.randomUUID().toString(),
        name = "My Process",
        tenantId = null
      ), correlations = correlationMap(dataEntry1), taskDefinitionKey = "task_1"
    )

    val json = mapper.writeValueAsString(task)

    val deserialized = mapper.readValue<Task>(json)

    assertThat(deserialized).isEqualTo(task)
  }

  @Test
  fun `can serialize and deserialize query with jackson`() {
    val query = DataEntriesForUserQuery(user = User("kermit", setOf("muppets")), filters = listOf("foo"))

    val json = mapper.writeValueAsString(query)

    val deserialized = mapper.readValue<DataEntriesForUserQuery>(json)

    assertThat(deserialized).isEqualTo(query)
  }

  @Test
  fun `can serialize and deserialize dataEntryQuery with jackson`() {
    val query = DataEntriesQuery(filters = listOf("foo"))

    val json = mapper.writeValueAsString(query)

    val deserialized = mapper.readValue<DataEntriesQuery>(json)

    assertThat(deserialized).isEqualTo(query)
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
  val value: String, val another: Int
)

data class MyStructure(
  val name: String, val variables: VariableMap, val source: SourceReference
)
