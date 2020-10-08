package io.holunda.camunda.taskpool.upcast.definition

import io.holunda.camunda.taskpool.upcast.definition.AnnotatedEventUpcaster.Companion.NULL_VALUE
import org.axonframework.serialization.SimpleSerializedType
import org.axonframework.serialization.upcasting.event.IntermediateEventRepresentation
import org.axonframework.serialization.upcasting.event.SingleEventUpcaster
import org.dom4j.Document
import org.springframework.stereotype.Component
import kotlin.reflect.full.findAnnotation

/**
 * Annotation based event up-caster for single event.
 * Sub-classes should annotate with [AnnotatedEventUpcaster].
 */
abstract class AnnotationBasedSingleEventUpcaster : SingleEventUpcaster() {

  @Suppress("MemberVisibilityCanBePrivate")
  val targetType = extractAnnotatedEventType()

  override fun canUpcast(representation: IntermediateEventRepresentation): Boolean =
    targetType == representation.type && representation.contentType() == representationContentType()

  internal fun extractAnnotatedEventType(): SimpleSerializedType {
    val annotation = annotation()
    return SimpleSerializedType(annotation.targetClassTypeName, if (annotation.revision != NULL_VALUE) annotation.revision else null)
  }

  private fun representationContentType() = annotation().representationContentType

  private fun annotation() = this::class.findAnnotation<AnnotatedEventUpcaster>()
    ?: throw IllegalStateException("Sub-classes of ${AnnotationBasedSingleEventUpcaster::class.simpleName} must be annotated with ${AnnotatedEventUpcaster::class.simpleName}")
}

/**
 * Marks a single event upcaster.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
@MustBeDocumented
@Component
annotation class AnnotatedEventUpcaster(
  /**
   * Target class type name.
   */
  val targetClassTypeName: String,
  /**
   * Target revision, defaults to reserved value [NULL_VALUE] to indicate <code>null</code>.
   */
  val revision: String = NULL_VALUE,

  /**
   * Representation content type.
   */
  val representationContentType: RepresentationContentType = RepresentationContentType.XML
) {
  companion object {
    const val NULL_VALUE = "SINGLE_EVENT_UPCASTER_NULL_VALUE"
  }
}

/**
 * Determines the content type based on the class of content type.
 */
fun IntermediateEventRepresentation.contentType(): RepresentationContentType {
  when (this.data.data) {
    is Document -> {
      return RepresentationContentType.XML
    }
    is String -> {
      return RepresentationContentType.JSON
    }
    is ByteArray -> {
      val asString = String(this.data.data as ByteArray)
      if (asString.startsWith("<")) {
        return RepresentationContentType.XML
      } else if (asString.startsWith("{")) {
        return RepresentationContentType.JSON
      }
    }
  }
  throw IllegalStateException("Unknown content type of ${this.data.data}")
}

/**
 * Supported content typess for this upcaster.
 */
enum class RepresentationContentType {
  JSON, XML
}
