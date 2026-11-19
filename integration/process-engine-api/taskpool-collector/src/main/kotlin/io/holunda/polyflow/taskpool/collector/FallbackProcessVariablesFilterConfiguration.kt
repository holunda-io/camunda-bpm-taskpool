package io.holunda.polyflow.taskpool.collector

import io.holunda.polyflow.taskpool.collector.task.enricher.ProcessVariablesFilter
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean

/**
 * Configures fallback if no process variable filter is defined.
 * No @Configuration required.
 * Configuration used via auto-configuration.
 */
@ConditionalOnMissingBean(ProcessVariablesFilter::class)
class FallbackProcessVariablesFilterConfiguration {
  /**
   * Empty process variable filter.
   */
  @Bean
  fun processVariablesFilterFallback(): ProcessVariablesFilter = ProcessVariablesFilter()
}
