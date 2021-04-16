package io.holunda.camunda.taskpool.api.task

import io.holunda.camunda.taskpool.api.business.WithCorrelations

/**
 * Task identity which can be enriched with payload and correlations.
 */
interface TaskIdentityWithPayloadAndCorrelations : TaskIdentity, WithPayload, WithCorrelations {
  /**
   * Flag, indicating if the enrichment has been performed. The payload is only then taken into account.
   */
  var enriched: Boolean
}
