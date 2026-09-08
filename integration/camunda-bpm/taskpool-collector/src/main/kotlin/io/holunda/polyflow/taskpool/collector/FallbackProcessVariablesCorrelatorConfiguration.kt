package io.holunda.polyflow.taskpool.collector

import io.holunda.polyflow.taskpool.collector.task.enricher.ProcessVariablesCorrelator
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean

/**
 * Configures fallback if no process variable correlator is defined.
 */
@AutoConfiguration
@ConditionalOnMissingBean(ProcessVariablesCorrelator::class)
class FallbackProcessVariablesCorrelatorConfiguration {
  /**
   * Creates the empty correlator used when neither a custom nor property-configured correlator is available.
   *
   * @return a correlator with no process-variable correlation definitions.
   */
  @Bean
  fun processVariablesCorrelatorFallback(): ProcessVariablesCorrelator = ProcessVariablesCorrelator()
}
