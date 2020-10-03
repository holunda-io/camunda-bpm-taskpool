package io.holunda.camunda.taskpool.gateway.io.holunda.camunda.taskpool.gateway

import org.axonframework.messaging.MetaData

/**
 * Represents query parameters with target revision and optional timeout.
 * @param minimalRevision target revision the query is checking for.
 * @param timeout optional timeout
 */
data class RevisionQueryParameters(
  val minimalRevision: Long,
  val timeout: Long? = null
) {

  companion object {

    const val REVISION_KEY = "projectionRevision"
    const val QUERY_TIMEOUT_KEY = "queryTimeout"

    val NOT_PRESENT = RevisionQueryParameters(Long.MIN_VALUE, null)

    /**
     * Reads revision parameters from metadata (of the query)
     */
    fun fromMetaData(metaData: MetaData): RevisionQueryParameters {
      val revision = if (metaData.containsKey(REVISION_KEY) && metaData[REVISION_KEY] is Long) {
        metaData[REVISION_KEY] as Long
      } else {
        return NOT_PRESENT
      }
      val queryTimeout = if (metaData.containsKey(QUERY_TIMEOUT_KEY) && metaData[QUERY_TIMEOUT_KEY] is Long) {
        metaData[QUERY_TIMEOUT_KEY] as Long
      } else {
        null
      }
      return RevisionQueryParameters(minimalRevision = revision, timeout = queryTimeout)
    }
  }

  /**
   * Get the timeout from the query and externally provided default.
   * @return timeout for the query to execute.
   */
  fun getTimeoutOrDefault(defaultTimeout: Long): Long =
    timeout ?: defaultTimeout

  /**
   * Creates meta data out of parameters.
   * @return metadata
   */
  fun toMetaData(): MetaData = MetaData.with(REVISION_KEY, this.minimalRevision).also {
    if (this.timeout != null) {
      it.and(QUERY_TIMEOUT_KEY, this.timeout)
    }
  }
}
