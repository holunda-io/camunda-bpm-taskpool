package io.holunda.camunda.taskpool.upcast

import io.holunda.camunda.taskpool.upcast.AnnotatedEventUpcaster.Companion.NULL_VALUE
import org.axonframework.serialization.SimpleSerializedType
import org.axonframework.serialization.upcasting.event.IntermediateEventRepresentation
import org.axonframework.serialization.upcasting.event.SingleEventUpcaster
import org.dom4j.Document
import kotlin.reflect.full.findAnnotation

/**
 * Annotation based event up-caster for single event.
 * Sub-classes should annotate with [AnnotatedEventUpcaster].
 */
abstract class AnnotationBasedSingleEventUpcaster : SingleEventUpcaster() {

  @Suppress("MemberVisibilityCanBePrivate")
  val targetType = extractAnnotatedEventType()

  // TODO: maybe check revision already here?
  override fun canUpcast(representation: IntermediateEventRepresentation): Boolean =
    targetType == representation.type && representation.contentType() == representationContentType()

  internal fun extractAnnotatedEventType(): SimpleSerializedType {
    val annotation = annotation()
    return SimpleSerializedType(annotation.targetClassTypeName, if (annotation.revision != NULL_VALUE) annotation.revision else null)
  }

  private fun representationContentType() = annotation().representationContentType

  private fun annotation() = this::class.findAnnotation<AnnotatedEventUpcaster>()
    ?: throw IllegalStateException("Sub-classes of ${AnnotationBasedSingleEventUpcaster::class.simpleName} must be annotated with ${AnnotatedEventUpcaster::class.simpleName}")

  /**
   * Determines the content type based on the class of content type.
   */
  private fun IntermediateEventRepresentation.contentType(): RepresentationContentType {
    when (this.data.data) {
      is Document -> {
        return RepresentationContentType.XML
      }

      is String -> {
        return RepresentationContentType.JSON
      }

      is ByteArray -> {
        // TODO: this seems expensive, maybe a check for the first byte is faster?
        // what about encoding?
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
}


