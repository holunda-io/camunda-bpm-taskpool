package io.holunda.camunda.taskpool.upcast.task

import io.holunda.camunda.taskpool.upcast.AnnotatedEventUpcaster
import io.holunda.camunda.taskpool.upcast.AnnotationBasedSingleEventUpcaster
import mu.KLogging
import org.axonframework.serialization.SimpleSerializedType
import org.axonframework.serialization.upcasting.event.IntermediateEventRepresentation
import org.dom4j.Document

/**
 * Upcaster fixing payload and correlation changes introduced by #305, by adding the missing attributes to XML.
 */
@AnnotatedEventUpcaster(TaskAttributeUpdatedEngineEvent4To5Upcaster.RESULT_OBJECT_TYPE, "4")
class TaskAttributeUpdatedEngineEvent4To5Upcaster : AnnotationBasedSingleEventUpcaster() {

  companion object : KLogging() {
    const val RESULT_OBJECT_TYPE = "io.holunda.camunda.taskpool.api.task.TaskAttributeUpdatedEngineEvent"
  }

  init {
    logger.debug { "EVENT-UPCASTER-001: Activating ${this::class.simpleName} for $RESULT_OBJECT_TYPE" }
  }

  override fun doUpcast(representation: IntermediateEventRepresentation): IntermediateEventRepresentation {
    return representation.upcastPayload(
      SimpleSerializedType(RESULT_OBJECT_TYPE, "5"),
      Document::class.java
    ) { document ->
      document.apply {
        if (document.selectNodes("//correlations").isEmpty()) {
          document.rootElement.addElement("correlations").apply {
            addAttribute("class", "org.camunda.bpm.engine.variable.impl.VariableMapImpl")
            addElement("variables")
          }
        }
        if (document.selectNodes("//payload").isEmpty()) {
          document.rootElement.addElement("payload").apply {
            addAttribute("class", "org.camunda.bpm.engine.variable.impl.VariableMapImpl")
            addElement("variables")
          }
        }
      }
    }
  }
}
