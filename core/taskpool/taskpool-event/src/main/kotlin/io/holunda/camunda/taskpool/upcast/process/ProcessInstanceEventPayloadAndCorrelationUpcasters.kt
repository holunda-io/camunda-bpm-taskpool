package io.holunda.camunda.taskpool.upcast.process

import io.holunda.camunda.taskpool.upcast.AnnotatedEventUpcaster
import io.holunda.camunda.taskpool.upcast.AnnotationBasedSingleEventUpcaster
import org.axonframework.serialization.SimpleSerializedType
import org.axonframework.serialization.upcasting.event.IntermediateEventRepresentation
import org.dom4j.Document

/**
 * Adds empty payload and correlation maps to historic process-instance event XML.
 */
abstract class ProcessInstanceEventPayloadAndCorrelationUpcaster(
  private val eventType: String,
  private val targetRevision: String,
) : AnnotationBasedSingleEventUpcaster() {
  override fun doUpcast(representation: IntermediateEventRepresentation): IntermediateEventRepresentation =
    representation.upcastPayload(SimpleSerializedType(eventType, targetRevision), Document::class.java) { document ->
      document.apply {
        if (selectNodes("//correlations").isEmpty()) {
          rootElement.addElement("correlations").apply {
            addAttribute("class", "org.camunda.bpm.engine.variable.impl.VariableMapImpl")
            addElement("variables")
          }
        }
        if (selectNodes("//payload").isEmpty()) {
          rootElement.addElement("payload").apply {
            addAttribute("class", "org.camunda.bpm.engine.variable.impl.VariableMapImpl")
            addElement("variables")
          }
        }
      }
    }
}

/**
 * Upcasts revision 1 process-started events to revision 2.
 */
@AnnotatedEventUpcaster(ProcessInstanceStartedEvent1To2Upcaster.EVENT_TYPE, "1")
class ProcessInstanceStartedEvent1To2Upcaster : ProcessInstanceEventPayloadAndCorrelationUpcaster(EVENT_TYPE, "2") {
  /**
   * Constants used by the process-started event upcaster.
   */
  companion object { const val EVENT_TYPE = "io.holunda.camunda.taskpool.api.process.instance.ProcessInstanceStartedEvent" }
}

/**
 * Upcasts revision 2 process-ended events to revision 3.
 */
@AnnotatedEventUpcaster(ProcessInstanceEndedEvent2To3Upcaster.EVENT_TYPE, "2")
class ProcessInstanceEndedEvent2To3Upcaster : ProcessInstanceEventPayloadAndCorrelationUpcaster(EVENT_TYPE, "3") {
  /**
   * Constants used by the process-ended event upcaster.
   */
  companion object { const val EVENT_TYPE = "io.holunda.camunda.taskpool.api.process.instance.ProcessInstanceEndedEvent" }
}

/**
 * Upcasts revision 1 process-cancelled events to revision 2.
 */
@AnnotatedEventUpcaster(ProcessInstanceCancelledEvent1To2Upcaster.EVENT_TYPE, "1")
class ProcessInstanceCancelledEvent1To2Upcaster : ProcessInstanceEventPayloadAndCorrelationUpcaster(EVENT_TYPE, "2") {
  /**
   * Constants used by the process-cancelled event upcaster.
   */
  companion object { const val EVENT_TYPE = "io.holunda.camunda.taskpool.api.process.instance.ProcessInstanceCancelledEvent" }
}

/**
 * Upcasts revision 1 process-suspended events to revision 2.
 */
@AnnotatedEventUpcaster(ProcessInstanceSuspendedEvent1To2Upcaster.EVENT_TYPE, "1")
class ProcessInstanceSuspendedEvent1To2Upcaster : ProcessInstanceEventPayloadAndCorrelationUpcaster(EVENT_TYPE, "2") {
  /**
   * Constants used by the process-suspended event upcaster.
   */
  companion object { const val EVENT_TYPE = "io.holunda.camunda.taskpool.api.process.instance.ProcessInstanceSuspendedEvent" }
}

/**
 * Upcasts revision 1 process-resumed events to revision 2.
 */
@AnnotatedEventUpcaster(ProcessInstanceResumedEvent1To2Upcaster.EVENT_TYPE, "1")
class ProcessInstanceResumedEvent1To2Upcaster : ProcessInstanceEventPayloadAndCorrelationUpcaster(EVENT_TYPE, "2") {
  /**
   * Constants used by the process-resumed event upcaster.
   */
  companion object { const val EVENT_TYPE = "io.holunda.camunda.taskpool.api.process.instance.ProcessInstanceResumedEvent" }
}
