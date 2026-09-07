package io.holunda.polyflow.taskpool.collector

import io.holunda.polyflow.taskpool.collector.task.enricher.ProcessVariableCorrelation
import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Properties for the property-backed [io.holunda.polyflow.taskpool.collector.task.enricher.ProcessVariablesCorrelator].
 */
@ConfigurationProperties("polyflow.integration.collector.camunda.process-variables-correlator")
data class ProcessVariablesCorrelatorProperties(
  /** Enables creation of the property-backed correlator. */
  val enabled: Boolean = false,
  /** Correlations, one entry for each process definition key. */
  val correlations: List<ProcessVariableCorrelation> = emptyList()
)
