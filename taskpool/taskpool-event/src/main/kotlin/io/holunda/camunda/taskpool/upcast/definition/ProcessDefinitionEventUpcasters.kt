package io.holunda.camunda.taskpool.upcast.definition

import com.fasterxml.jackson.databind.ObjectMapper
import org.axonframework.serialization.SimpleSerializedType
import org.axonframework.serialization.upcasting.event.IntermediateEventRepresentation
import java.util.function.Function


@AnnotatedEventUpcaster("io.holunda.camunda.taskpool.api.task.ProcessDefinitionRegisteredEvent")
class ProcessDefinitionEventNullTo1Upcaster(val objectMapper: ObjectMapper) : AnnotationBasedSingleEventUpcaster() {

  override fun doUpcast(representation: IntermediateEventRepresentation): IntermediateEventRepresentation {
    return representation.upcastPayload(
      SimpleSerializedType("io.holunda.camunda.taskpool.api.process.definition.ProcessDefinitionRegisteredEvent", "1"),
      String::class.java,
      Function.identity<String>()
    )
  }
}
