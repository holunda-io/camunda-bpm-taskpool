package io.holunda.camunda.taskpool.upcast

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.holunda.camunda.taskpool.api.process.definition.ProcessDefinitionRegisteredEvent
import io.holunda.camunda.taskpool.upcast.definition.ProcessDefinitionEventJSONNullTo1Upcaster
import io.holunda.camunda.taskpool.upcast.definition.ProcessDefinitionEventXMLNullTo1Upcaster
import org.assertj.core.api.Assertions.assertThat
import org.axonframework.eventhandling.EventData
import org.axonframework.messaging.MetaData
import org.axonframework.serialization.SerializedObject
import org.axonframework.serialization.SimpleSerializedObject
import org.axonframework.serialization.SimpleSerializedType
import org.axonframework.serialization.json.JacksonSerializer
import org.axonframework.serialization.upcasting.event.EventUpcasterChain
import org.axonframework.serialization.upcasting.event.InitialEventRepresentation
import org.axonframework.serialization.upcasting.event.IntermediateEventRepresentation
import org.axonframework.serialization.xml.XStreamSerializer
import org.dom4j.Document
import org.dom4j.DocumentHelper
import org.dom4j.io.SAXReader
import org.junit.Ignore
import org.junit.Test
import java.io.StringReader
import java.time.Instant
import java.util.*
import java.util.stream.Stream
import kotlin.streams.toList

class ProcessDefinitionUpcasterTest {

  @Test
  fun shouldUpcastJackson() {

    val json = """
        {
          "processDefinitionId": "id",
          "processDefinitionKey": "key",
          "processDefinitionVersion": 0,
          "applicationName": "taskpool",
          "processName": "My name",
          "processVersionTag": null,
          "processDescription": "description",
          "formKey": null,
          "startableFromTasklist": false,
          "candidateStarterUsers": [],
          "candidateStarterGroups": []
        }
    """.trimIndent()

    val serializer = JacksonSerializer.builder().objectMapper(jacksonObjectMapper()).build()

    val entry: EventData<String> = SimpleEventData<String>(
      metaData = SimpleSerializedObject("{}", String::class.java, SimpleSerializedType(MetaData::class.java.name, null)),
      payload = SimpleSerializedObject(json, String::class.java, SimpleSerializedType("io.holunda.camunda.taskpool.api.task.ProcessDefinitionRegisteredEvent", null))
    )
    val eventStream: Stream<IntermediateEventRepresentation> = Stream.of(
      InitialEventRepresentation(
        entry,
        serializer
      )
    )
    val upcaster = EventUpcasterChain(
      ProcessDefinitionEventJSONNullTo1Upcaster(),
    )
    val result = upcaster.upcast(eventStream).toList()
    val event: ProcessDefinitionRegisteredEvent = serializer.deserialize(result[0].data)
    assertThat(event).isNotNull
  }

  @Test
  fun shouldUpcastXStream() {

    val xml = """
      <io.holunda.camunda.taskpool.api.task.ProcessDefinitionRegisteredEvent>
          <processDefinitionId>id</processDefinitionId>
          <processDefinitionKey>key</processDefinitionKey>
          <processDefinitionVersion>0</processDefinitionVersion>
          <applicationName>taskpool</applicationName>
          <processName>My name</processName>
          <processDescription>description</processDescription>
          <startableFromTasklist>false</startableFromTasklist>
          <candidateStarterUsers/>
          <candidateStarterGroups/>
      </io.holunda.camunda.taskpool.api.task.ProcessDefinitionRegisteredEvent>
    """.trimIndent()

    val document = SAXReader().read(StringReader(xml))
    val clazz = Document::class.java
    val serializer = XStreamSerializer.defaultSerializer()

    val entry: EventData<Document> = SimpleEventData<Document>(
      metaData = SimpleSerializedObject(DocumentHelper.createDocument(), clazz, SimpleSerializedType(MetaData::class.java.name, null)),
      payload = SimpleSerializedObject(document, clazz, SimpleSerializedType("io.holunda.camunda.taskpool.api.task.ProcessDefinitionRegisteredEvent", null))
    )
    val eventStream: Stream<IntermediateEventRepresentation> = Stream.of(
      InitialEventRepresentation(
        entry,
        serializer
      )
    )
    val upcaster = EventUpcasterChain(ProcessDefinitionEventXMLNullTo1Upcaster())
    val result = upcaster.upcast(eventStream).toList()
    val event: ProcessDefinitionRegisteredEvent = serializer.deserialize(result[0].data)

    assertThat(event).isNotNull

  }

}


data class SimpleEventData<T : Any>(
  private val eventIdentifier: String = UUID.randomUUID().toString(),
  private val timestamp: Instant = Instant.now(),
  private val metaData: SerializedObject<T>,
  private val payload: SerializedObject<T>
) : EventData<T> {
  override fun getEventIdentifier(): String = eventIdentifier
  override fun getTimestamp(): Instant = timestamp
  override fun getPayload(): SerializedObject<T> = payload
  override fun getMetaData(): SerializedObject<T> = metaData
}
