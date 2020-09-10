package io.holunda.camunda.taskpool.upcast.definition

import io.holunda.camunda.taskpool.upcast.definition.AnnotatedEventUpcaster.Companion.NULL_VALUE
import org.axonframework.serialization.SimpleSerializedType
import org.axonframework.serialization.upcasting.event.IntermediateEventRepresentation
import org.axonframework.serialization.upcasting.event.SingleEventUpcaster
import kotlin.reflect.full.findAnnotation

/**
 * Annotation based event up-caster for single event.
 * Sub-classes should annotate with [AnnotatedEventUpcaster].
 */
abstract class AnnotationBasedSingleEventUpcaster : SingleEventUpcaster() {

  @Suppress("MemberVisibilityCanBePrivate")
  val targetType = extractAnnotatedEventType()

  override fun canUpcast(representation: IntermediateEventRepresentation): Boolean =
    targetType == representation.type

  private fun extractAnnotatedEventType(): SimpleSerializedType {
    val annotation = this::class.findAnnotation<AnnotatedEventUpcaster>()
      ?: throw IllegalStateException("Sub-classes of ${AnnotationBasedSingleEventUpcaster::class.simpleName} must be annotated with ${AnnotatedEventUpcaster::class.simpleName}")

    return SimpleSerializedType(annotation.targetClassTypeName, if (annotation.revision != NULL_VALUE) annotation.revision else null)
  }
}

/**
 * Marks a single event upcaster.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
@MustBeDocumented
annotation class AnnotatedEventUpcaster(
  /**
   * Target class type name.
   */
  val targetClassTypeName: String,
  /**
   * Target revision, defaults to reserved value [NULL_VALUE] to indicate <code>null</code>.
   */
  val revision: String = NULL_VALUE
) {
  companion object {
    const val NULL_VALUE = "SINGLE_EVENT_UPCASTER_NULL_VALUE"
  }
}
