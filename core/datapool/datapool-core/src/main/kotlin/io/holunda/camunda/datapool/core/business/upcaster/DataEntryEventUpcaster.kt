package io.holunda.camunda.datapool.core.business.upcaster

import io.holunda.camunda.taskpool.api.business.DataEntryCreatedEvent
import org.axonframework.serialization.SimpleSerializedType
import org.axonframework.serialization.upcasting.event.IntermediateEventRepresentation
import org.axonframework.serialization.upcasting.event.SingleEventUpcaster
import org.dom4j.Document
import org.dom4j.DocumentHelper
import org.springframework.stereotype.Component

/**
 * Upcaster adding attributes applicationName, type and name to the event as specified in revision 2.
 */
@Component
class DataEntryCreatedEventUpcaster : SingleEventUpcaster() {

  public override fun canUpcast(ir: IntermediateEventRepresentation): Boolean =
    ir.type.name == DataEntryCreatedEvent::class.qualifiedName &&
      ir.type.revision == null


  override fun doUpcast(ir: IntermediateEventRepresentation): IntermediateEventRepresentation =
    ir.upcastPayload(
      SimpleSerializedType(DataEntryCreatedEvent::class.qualifiedName, "2"),
      Document::class.java
    ) {
      it.apply {
        this.rootElement.add(DocumentHelper.createElement("applicationName").apply {
          this.add(DocumentHelper.createText("unknown"))
        })
        this.rootElement.add(DocumentHelper.createElement("type").apply {
          this.add(DocumentHelper.createText("unknown"))
        })
        this.rootElement.add(DocumentHelper.createElement("name").apply {
          this.add(DocumentHelper.createText("unknown"))
        })
      }
    }
}
