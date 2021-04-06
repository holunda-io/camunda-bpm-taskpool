package io.holunda.camunda.taskpool

import io.holunda.camunda.taskpool.collector.task.enricher.ProcessVariablesCorrelator
import io.holunda.camunda.taskpool.collector.task.enricher.ProcessVariablesFilter
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * No @Configuration required.
 * Configuration used via auto-configuration.
 */
@ConditionalOnMissingBean(ProcessVariablesFilter::class)
@Configuration
class FallbackProcessVariablesFilterConfiguration {
  /**
   * Empty process variable filter.
   */
  @Bean
  fun processVariablesFilterFallback(): ProcessVariablesFilter = ProcessVariablesFilter()
}

/**
 * No @Configuration required.
 * Configuration used via auto-configuration.
 */
@Configuration
@ConditionalOnMissingBean(ProcessVariablesCorrelator::class)
class FallbackProcessVariablesCorrelatorConfiguration {
  /**
   * Empty correlator.
   */
  @Bean
  fun processVariablesCorrelatorFallback(): ProcessVariablesCorrelator = ProcessVariablesCorrelator()
}
