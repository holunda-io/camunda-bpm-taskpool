package io.holunda.camunda.taskpool.gateway.io.holunda.camunda.taskpool.gateway

import org.axonframework.messaging.MetaData

/**
 * Represents projection revision (used in results).
 * @see [Revisionable]
 */
data class RevisionValue(
  val revision: Long
): Comparable<RevisionValue> {
  companion object {
    const val REVISION_KEY = "projectionRevision"

    /**
     * No revision.
     */
    val NO_REVISION = RevisionValue(Long.MIN_VALUE)

    /**
     * Reads revision parameters from metadata (of the query)
     */
    fun fromMetaData(metaData: MetaData): RevisionValue {
      return  if (metaData.containsKey(REVISION_KEY) && metaData[REVISION_KEY] is Long) {
          RevisionValue(revision = metaData[RevisionQueryParameters.REVISION_KEY] as Long)
      } else {
        NO_REVISION
      }
    }
  }

  /**
   * Creates meta data out of parameter.
   * @return metadata
   */
  fun toMetaData(): MetaData = MetaData.with(REVISION_KEY, this.revision)

  override fun compareTo(other: RevisionValue): Int = this.revision.compareTo(other.revision)
}
