package io.holunda.camunda.taskpool.upcast.definition

import org.axonframework.serialization.SimpleSerializedType
import org.axonframework.serialization.upcasting.event.IntermediateEventRepresentation
import org.xml.sax.InputSource
import java.io.StringReader
import java.io.StringWriter
import java.util.function.Function
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult


@AnnotatedEventUpcaster("io.holunda.camunda.taskpool.api.task.ProcessDefinitionRegisteredEvent")
class ProcessDefinitionEventNullTo1Upcaster : AnnotationBasedSingleEventUpcaster() {

  companion object {
    const val RESULT_OBJECT_TYPE = "io.holunda.camunda.taskpool.api.process.definition.ProcessDefinitionRegisteredEvent"
  }

  override fun doUpcast(representation: IntermediateEventRepresentation): IntermediateEventRepresentation {
    return representation.upcastPayload(
      SimpleSerializedType(RESULT_OBJECT_TYPE, "1"),
      String::class.java,
      when (representation.contentType()) {
        // Jackson is not writing class names inside the event.
        RepresentationContentType.JSON -> Function.identity<String>()
        // XStream writes the event types into the event.
        RepresentationContentType.XML -> Function {
          val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(InputSource(StringReader(it)))
          val nodes = document.getElementsByTagName(extractAnnotatedEventType().name) // use the type name from the annotation
          for (i: Int in 0 until nodes.length) {
            document.renameNode(nodes.item(i), null, RESULT_OBJECT_TYPE)
          }
          val transformer = TransformerFactory.newInstance().newTransformer()
          val writer = StringWriter()
          transformer.transform(DOMSource(document), StreamResult(writer))
          writer.buffer.toString()
        }
      }
    )
  }
}

/**
 * Determines the content type based on the first character of the content.
 */
fun IntermediateEventRepresentation.contentType(): RepresentationContentType {
  if (this.data.data.toString().startsWith("<")) {
    return RepresentationContentType.XML
  } else if (this.data.data.toString().startsWith("{")) {
    return RepresentationContentType.JSON
  }
  throw IllegalStateException("Unknown content type")
}

/**
 * Supported content typess for this upcaster.
 */
enum class RepresentationContentType {
  JSON, XML
}
