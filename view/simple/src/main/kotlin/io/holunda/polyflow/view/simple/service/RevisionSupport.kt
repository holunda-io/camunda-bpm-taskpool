package io.holunda.polyflow.view.simple.service

import io.github.oshai.kotlinlogging.KotlinLogging
import io.holixon.axon.gateway.query.RevisionValue
import java.util.concurrent.ConcurrentHashMap

private val logger = KotlinLogging.logger {}

/**
 * Helper to create revision supported projection.
 */
class RevisionSupport {

  private val revisionInfo = ConcurrentHashMap<String, RevisionValue>()

  /**
   * Updates the revision for specified element key.
   * @param elementKey key of the element
   * @param revisionValue new revision value.
   */
  fun updateRevision(elementKey: String, revisionValue: RevisionValue) {
    // store latest revision for data entry, if any, but don't overwrite an existing with "no revision"
    if (revisionValue != RevisionValue.NO_REVISION) {
      val currentRevision = getRevisionMax(setOf(elementKey))
      // only increment revision
      if (revisionValue > currentRevision) {
        logger.trace { "SIMPLE-VIEW-41: Revision updated for $elementKey to ${revisionValue.revision}" }
        revisionInfo[elementKey] = revisionValue
      } else {
        logger.warn { "SIMPLE-VIEW-42: Skipping revision update for $elementKey to ${revisionValue.revision}, since it is NOT higher than current: ${currentRevision.revision}." }
      }
    }
  }

  /**
   * Deletes revision information for this entry.
   */
  fun deleteRevision(elementKey: String) {
    revisionInfo.remove(elementKey)
  }

  /**
   * Retrieve the highest revision for given set of element keys.
   * @param elementKeys keys of elements.
   * @return highest revision value.
   */
  fun getRevisionMax(elementKeys: Collection<String>): RevisionValue {
    return (
      revisionInfo
        .filter { elementKeys.contains(it.key) }
        .maxByOrNull { it.value }?.value
        ?: RevisionValue.NO_REVISION
      ).also {
        logger.trace { "SIMPLE-VIEW-43: Retrieving revision for $elementKeys, the result is $it" }
      }
  }
}
