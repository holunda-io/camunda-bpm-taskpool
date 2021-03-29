package io.holunda.camunda.taskpool.upcast

import io.holunda.camunda.taskpool.api.task.*
import io.holunda.camunda.taskpool.upcast.task.*
import org.assertj.core.api.Assertions.assertThat
import org.axonframework.eventhandling.EventData
import org.axonframework.messaging.MetaData
import org.axonframework.serialization.SimpleSerializedObject
import org.axonframework.serialization.SimpleSerializedType
import org.axonframework.serialization.upcasting.event.EventUpcasterChain
import org.axonframework.serialization.upcasting.event.InitialEventRepresentation
import org.axonframework.serialization.upcasting.event.IntermediateEventRepresentation
import org.axonframework.serialization.xml.XStreamSerializer
import org.dom4j.Document
import org.dom4j.DocumentHelper
import org.dom4j.io.SAXReader
import org.junit.Test
import java.io.StringReader
import java.util.stream.Stream
import kotlin.reflect.KClass
import kotlin.streams.toList

class TaskUpdateEventPayloadAndCorrelationUpcasterTest {

  @Test
  fun shouldUpcastXStream() {
    upcastAndReturn(TaskAttributeUpdatedEngineEvent::class).apply {
      assertThat(this.correlations).isNotNull
      assertThat(this.payload).isNotNull
    }
  }

  /**
   * Run the test with the specified version of the event.
   */
  private inline fun <reified T : Any> upcastAndReturn(eventClazz: KClass<T>) : T {
    val eventName: String = eventClazz.qualifiedName!!
    val xml = generateFakeOldEventXML(eventClazz)
    val document = SAXReader().read(StringReader(xml))
    val clazz = org.dom4j.Document::class.java
    val serializer = XStreamSerializer.builder().lenientDeserialization().build()

    val entry: EventData<Document> = SimpleEventData<Document>(
      metaData = SimpleSerializedObject(DocumentHelper.createDocument(), clazz, SimpleSerializedType(MetaData::class.java.name, null)),
      payload = SimpleSerializedObject(document, clazz, SimpleSerializedType(eventName, "4"))
    )
    val eventStream: Stream<IntermediateEventRepresentation> = Stream.of(
      InitialEventRepresentation(
        entry,
        serializer
      )
    )
    val upcaster = EventUpcasterChain(
      TaskAttributeUpdatedEngineEvent4To5Upcaster(),
    )
    val result = upcaster.upcast(eventStream).toList()
    val event: T = serializer.deserialize(result[0].data)

    assertThat(event).isNotNull
    return event
  }


  /**
   * This method generates the relevant part of the old event. Most other fields are empty, but the source reference is filled.
   */
  private fun <T : Any> generateFakeOldEventXML(clazz: KClass<T>): String {
    return when (clazz) {
      TaskAttributeUpdatedEngineEvent::class,
      ->
        """
        <${clazz.qualifiedName}>
          <eventType>create</eventType>
          <id>13a16b8c-097d-11eb-a8e3-ced136cca53a</id>
          <sourceReference class="io.holunda.camunda.taskpool.api.task.ProcessReference">
            <instanceId>1392031f-097d-11eb-a8e3-ced136cca53a</instanceId>
            <executionId>13a1447a-097d-11eb-a8e3-ced136cca53a</executionId>
            <definitionId>process_approve_request:1:097ada09-097d-11eb-a8e3-ced136cca53a</definitionId>
            <definitionKey>process_approve_request</definitionKey>
            <name>Request Approval</name>
            <applicationName>example-process-approval</applicationName>
          </sourceReference>
          <taskDefinitionKey>user_approve_request</taskDefinitionKey>
        </${clazz.qualifiedName}>
        """.trimIndent()
      else -> throw IllegalArgumentException("Unsupported event of type ${clazz.qualifiedName}")
    }
  }
}
