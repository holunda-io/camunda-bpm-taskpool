package io.holunda.camunda.taskpool.view.simple.service

import io.holunda.camunda.taskpool.view.query.RevisionValue
import java.util.concurrent.ConcurrentHashMap

class RevisionSupport {

  private val revisionInfo = ConcurrentHashMap<String, RevisionValue>()

  /**
   * Updates the revision for specified element key.
   * @param elementKey key of the element
   * @param revisionValue new revision value.
   */
  fun updateRevision(elementKey: String, revisionValue: RevisionValue) {
    if (revisionValue != RevisionValue.NO_REVISION) {
      // store latest revision for data entry, if any, but don't overwrite an existing with "no revision"
      revisionInfo[elementKey] = revisionValue
    }
  }

  /**
   * Retrieve the highest revision for given set of element keys.
   * @param elementKeys keys of elements.
   * @return highest revision value.
   */
  fun getRevisionMax(elementKeys: Collection<String>): RevisionValue {
    return revisionInfo
      .filter { elementKeys.contains(it.key) }
      .maxByOrNull { it.value }?.value
      ?: RevisionValue.NO_REVISION
  }
}
