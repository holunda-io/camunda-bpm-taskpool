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
   * Empty correlator.
   */
  @Bean
  fun processVariablesCorrelatorFallback(): ProcessVariablesCorrelator = ProcessVariablesCorrelator()
}
