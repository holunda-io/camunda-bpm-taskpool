package io.holunda.camunda.taskpool.upcast

import com.thoughtworks.xstream.XStream
import com.thoughtworks.xstream.security.AnyTypePermission
import io.holunda.camunda.taskpool.upcast.process.ProcessInstanceCancelledEvent1To2Upcaster
import io.holunda.camunda.taskpool.upcast.process.ProcessInstanceEndedEvent2To3Upcaster
import io.holunda.camunda.taskpool.upcast.process.ProcessInstanceResumedEvent1To2Upcaster
import io.holunda.camunda.taskpool.upcast.process.ProcessInstanceStartedEvent1To2Upcaster
import io.holunda.camunda.taskpool.upcast.process.ProcessInstanceSuspendedEvent1To2Upcaster
import org.assertj.core.api.Assertions.assertThat
import org.axonframework.eventhandling.EventData
import org.axonframework.messaging.MetaData
import org.axonframework.serialization.SimpleSerializedObject
import org.axonframework.serialization.SimpleSerializedType
import org.axonframework.serialization.upcasting.event.EventUpcaster
import org.axonframework.serialization.upcasting.event.EventUpcasterChain
import org.axonframework.serialization.upcasting.event.InitialEventRepresentation
import org.dom4j.Document
import org.dom4j.DocumentHelper
import org.junit.jupiter.api.Test
import java.util.stream.Stream

class ProcessInstanceEventPayloadAndCorrelationUpcasterTest {

  @Test
  fun `adds empty enrichment maps to every historic process instance event`() {
    listOf(
      ProcessInstanceStartedEvent1To2Upcaster.EVENT_TYPE to "1",
      ProcessInstanceEndedEvent2To3Upcaster.EVENT_TYPE to "2",
      ProcessInstanceCancelledEvent1To2Upcaster.EVENT_TYPE to "1",
      ProcessInstanceSuspendedEvent1To2Upcaster.EVENT_TYPE to "1",
      ProcessInstanceResumedEvent1To2Upcaster.EVENT_TYPE to "1",
    ).forEach { (eventType, revision) ->
      val result = EventUpcasterChain(upcasters()).upcast(
        Stream.of(InitialEventRepresentation(eventData(eventType, revision), serializer()))
      ).findFirst().orElseThrow()

      val document = result.data.data as Document
      assertThat(document.selectNodes("//payload")).hasSize(1)
      assertThat(document.selectNodes("//correlations")).hasSize(1)
    }
  }

  private fun upcasters(): List<EventUpcaster> = listOf(
    ProcessInstanceStartedEvent1To2Upcaster(),
    ProcessInstanceEndedEvent2To3Upcaster(),
    ProcessInstanceCancelledEvent1To2Upcaster(),
    ProcessInstanceSuspendedEvent1To2Upcaster(),
    ProcessInstanceResumedEvent1To2Upcaster(),
  )

  private fun eventData(eventType: String, revision: String): EventData<Document> = SimpleEventData(
    metaData = SimpleSerializedObject(DocumentHelper.createDocument(), Document::class.java, SimpleSerializedType(MetaData::class.java.name, null)),
    payload = SimpleSerializedObject(DocumentHelper.parseText("<$eventType/>"), Document::class.java, SimpleSerializedType(eventType, revision)),
  )

  private fun serializer() = org.axonframework.serialization.xml.XStreamSerializer.builder()
    .xStream(XStream().apply { addPermission(AnyTypePermission.ANY) })
    .build()
}
