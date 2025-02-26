package io.holunda.camunda.taskpool.upcast.definition

import io.github.oshai.kotlinlogging.KotlinLogging
import io.holunda.camunda.taskpool.upcast.AnnotatedEventUpcaster
import io.holunda.camunda.taskpool.upcast.AnnotationBasedSingleEventUpcaster
import io.holunda.camunda.taskpool.upcast.RepresentationContentType.JSON
import org.axonframework.serialization.SimpleSerializedType
import org.axonframework.serialization.upcasting.event.IntermediateEventRepresentation
import org.dom4j.Document
import java.util.function.Function

private val logger = KotlinLogging.logger {}

/**
 * Upcaster to put revision 1 into the event and remove unneeded attributes.
 */
@AnnotatedEventUpcaster("io.holunda.camunda.taskpool.api.task.ProcessDefinitionRegisteredEvent")
class ProcessDefinitionEventXMLNullTo1Upcaster : AnnotationBasedSingleEventUpcaster() {

  companion object {
    const val RESULT_OBJECT_TYPE = "io.holunda.camunda.taskpool.api.process.definition.ProcessDefinitionRegisteredEvent"
  }

  init {
    logger.debug { "EVENT-UPCASTER-001: Activating ${this::class.simpleName} for $RESULT_OBJECT_TYPE" }
  }

  override fun doUpcast(representation: IntermediateEventRepresentation): IntermediateEventRepresentation {
    return representation.upcastPayload(
      SimpleSerializedType(RESULT_OBJECT_TYPE, "1"),
      Document::class.java
    )
    // XStream writes the event types into the event.
    { document ->
      document.apply {
        val nodes = this.selectNodes("//" + extractAnnotatedEventType().name) // use the type name from the annotation
        nodes.forEach { node ->
          node.name = RESULT_OBJECT_TYPE
        }
      }
    }
  }
}

/**
 * Upcaster to put revision 1 into the event and remove unneeded attributes.
 */
@AnnotatedEventUpcaster("io.holunda.camunda.taskpool.api.task.ProcessDefinitionRegisteredEvent", representationContentType = JSON)
class ProcessDefinitionEventJSONNullTo1Upcaster : AnnotationBasedSingleEventUpcaster() {

  companion object {
    const val RESULT_OBJECT_TYPE = "io.holunda.camunda.taskpool.api.process.definition.ProcessDefinitionRegisteredEvent"
  }

  init {
    logger.debug { "EVENT-UPCASTER-001: Activating ${this::class.simpleName} for $RESULT_OBJECT_TYPE" }
  }

  override fun doUpcast(representation: IntermediateEventRepresentation): IntermediateEventRepresentation {
    return representation.upcastPayload(
      SimpleSerializedType(RESULT_OBJECT_TYPE, "1"),
      String::class.java,
      // Jackson is not writing class names inside the event.
      Function.identity()
    )
  }
}
