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

  /**
   * Type of upcasted result.
   */
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

  /**
   * Declares the serialized type produced by this upcaster.
   */
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

/**
 * Upcasts registered process-definition events from revision 1 to revision 2 by adding an empty payload map.
 */
@AnnotatedEventUpcaster(ProcessDefinitionRegisteredEvent1To2Upcaster.EVENT_TYPE, "1")
class ProcessDefinitionRegisteredEvent1To2Upcaster : AnnotationBasedSingleEventUpcaster() {

  /**
   * Declares the serialized type produced by this upcaster.
   */
  companion object {
    const val EVENT_TYPE = "io.holunda.camunda.taskpool.api.process.definition.ProcessDefinitionRegisteredEvent"
  }

  /**
   * Adds an empty Camunda variable map if the historic event has no payload element.
   *
   * @param representation historic event representation.
   * @return revision-two event representation.
   */
  override fun doUpcast(representation: IntermediateEventRepresentation): IntermediateEventRepresentation =
    representation.upcastPayload(SimpleSerializedType(EVENT_TYPE, "2"), Document::class.java) { document ->
      document.apply {
        if (selectNodes("//payload").isEmpty()) {
          rootElement.addElement("payload").apply {
            addAttribute("class", "org.camunda.bpm.engine.variable.impl.VariableMapImpl")
            addElement("variables")
          }
        }
      }
    }
}
