package io.holunda.camunda.taskpool.upcast

import com.thoughtworks.xstream.XStream
import com.thoughtworks.xstream.security.AnyTypePermission
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
import org.junit.jupiter.api.Test
import java.io.StringReader
import java.util.stream.Collectors
import java.util.stream.Stream
import kotlin.reflect.KClass

// TODO: Move out test setup into separate project.
class TaskEventSourceReferenceUpcasterTest {

  @Test
  fun shouldUpcastXStream() {
    runTheTest(TaskCreatedEngineEvent::class, "3")
    runTheTest(TaskCompletedEngineEvent::class, "3")
    runTheTest(TaskAssignedEngineEvent::class, "3")
    runTheTest(TaskDeletedEngineEvent::class, "3")
    runTheTest(TaskAttributeUpdatedEngineEvent::class, "3")
    runTheTest(TaskCandidateGroupChanged::class, "1")
    runTheTest(TaskCandidateUserChanged::class, "1")
    runTheTest(TaskClaimedEvent::class, "2")
    runTheTest(TaskUnclaimedEvent::class, "2")
    runTheTest(TaskToBeCompletedEvent::class, "2")
    runTheTest(TaskDeferredEvent::class, "2")
    runTheTest(TaskUndeferredEvent::class, "2")
  }

  /**
   * Run the test with the specified version of the event.
   */
  private inline fun <reified T : Any> runTheTest(clazz: KClass<T>, revisionNumber: String) : T {
    val eventName: String = clazz.qualifiedName!!
    val xml = generateFakeOldEventXML(clazz)
    val document = SAXReader().read(StringReader(xml))
    val dataTypeClazz = org.dom4j.Document::class.java
    val serializer = XStreamSerializer.builder().xStream(XStream().apply { addPermission(AnyTypePermission.ANY) }).build()

    val entry: EventData<Document> = SimpleEventData<Document>(
      metaData = SimpleSerializedObject(DocumentHelper.createDocument(), dataTypeClazz, SimpleSerializedType(MetaData::class.java.name, null)),
      payload = SimpleSerializedObject(document, dataTypeClazz, SimpleSerializedType(eventName, revisionNumber))
    )
    val eventStream: Stream<IntermediateEventRepresentation> = Stream.of(
      InitialEventRepresentation(
        entry,
        serializer
      )
    )
    val upcaster = EventUpcasterChain(

      TaskCreatedEngineEvent3To4Upcaster(),
      TaskCompletedEngineEvent3To4Upcaster(),
      TaskDeletedEngineEvent3To4Upcaster(),
      TaskAttributeUpdatedEngineEvent3To4Upcaster(),
      TaskAssignedEngineEvent3To4Upcaster(),

      TaskCandidateGroupChanged1To4Upcaster(),
      TaskCandidateUserChanged1To4Upcaster(),

      TaskClaimedEvent2To4Upcaster(),
      TaskUnclaimedEvent2To4Upcaster(),
      TaskToBeCompletedEvent2To4Upcaster(),
      TaskDeferredEvent2To4Upcaster(),
      TaskUndeferredEvent2To4Upcaster(),

      )
    val result = upcaster.upcast(eventStream).collect(Collectors.toList())
    val event: T = serializer.deserialize(result[0].data)

    assertThat(event).isNotNull
    return event
  }


  /**
   * This method generates the relevant part of the old event. Most other fields are empty, but the source reference is filled.
   */
  private fun <T : Any> generateFakeOldEventXML(clazz: KClass<T>): String {
    return when (clazz) {
      TaskCreatedEngineEvent::class,
      TaskDeletedEngineEvent::class,
      TaskCompletedEngineEvent::class,
      TaskAssignedEngineEvent::class,
      TaskAttributeUpdatedEngineEvent::class,
      TaskCandidateGroupChanged::class,
      TaskCandidateUserChanged::class,
      TaskClaimedEvent::class,
      TaskUnclaimedEvent::class,
      TaskDeferredEvent::class,
      TaskUndeferredEvent::class,
      TaskToBeCompletedEvent::class,
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
            <instanceId defined-in="io.holunda.camunda.taskpool.api.task.SourceReference">1392031f-097d-11eb-a8e3-ced136cca53a</instanceId>
            <executionId defined-in="io.holunda.camunda.taskpool.api.task.SourceReference">13a1447a-097d-11eb-a8e3-ced136cca53a</executionId>
            <definitionId defined-in="io.holunda.camunda.taskpool.api.task.SourceReference">process_approve_request:1:097ada09-097d-11eb-a8e3-ced136cca53a</definitionId>
            <definitionKey defined-in="io.holunda.camunda.taskpool.api.task.SourceReference">process_approve_request</definitionKey>
            <name defined-in="io.holunda.camunda.taskpool.api.task.SourceReference">Request Approval</name>
            <applicationName defined-in="io.holunda.camunda.taskpool.api.task.SourceReference">example-process-approval</applicationName>
          </sourceReference>
          <taskDefinitionKey>user_approve_request</taskDefinitionKey>
        </${clazz.qualifiedName}>
        """.trimIndent()
      else -> throw IllegalArgumentException("Unsupported event of type ${clazz.qualifiedName}")
    }
  }
}
