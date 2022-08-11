package io.holunda.polyflow.datapool.core.business.upcaster

import com.thoughtworks.xstream.XStream
import org.assertj.core.api.Assertions.assertThat
import org.axonframework.eventhandling.GenericDomainEventEntry
import org.axonframework.serialization.upcasting.event.InitialEventRepresentation
import org.axonframework.serialization.xml.XStreamSerializer
import org.dom4j.Document
import org.dom4j.DocumentHelper.*
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*
import java.util.stream.Stream

class DataEntryCreatedEventUpcasterTest {

  private val upcaster = DataEntryCreatedEventUpcaster()

  @Test
  fun testUpcaster() {

    // GIVEN
    val data = GenericDomainEventEntry<Document>(
      "DataEntryAggregate",
      "72608CAB-B4",
      0,
      UUID.randomUUID().toString(),
      Instant.now(),
      "io.holunda.camunda.taskpool.api.business.DataEntryCreatedEvent",
      null,
      build_previous_event(),
      createDocument()
    )
    val init = InitialEventRepresentation(data, XStreamSerializer.builder().xStream(XStream()).build())

    // WHEN
    assertThat(upcaster.canUpcast(init)).isTrue
    val stream = upcaster.upcast(Stream.of(init))
    val result = stream.findAny()

    // THEN
    assertThat(result).isNotEmpty

    // FIXME: how should we test it?
  }

  private fun build_previous_event(): Document =
    createDocument(createElement("io.holunda.camunda.taskpool.api.business.DataEntryCreatedEvent")
      .apply {
        this.add(createElement("entryId").apply { add(createText("4711")) })
        this.add(createElement("entryType").apply { add(createText("io.some.Type")) })
        this.add(createElement("payload"))
        this.add(createElement("correlations"))
      }
    )

}

