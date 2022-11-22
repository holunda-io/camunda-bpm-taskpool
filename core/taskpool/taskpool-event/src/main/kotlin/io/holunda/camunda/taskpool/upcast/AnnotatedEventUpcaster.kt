package io.holunda.camunda.taskpool.upcast

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