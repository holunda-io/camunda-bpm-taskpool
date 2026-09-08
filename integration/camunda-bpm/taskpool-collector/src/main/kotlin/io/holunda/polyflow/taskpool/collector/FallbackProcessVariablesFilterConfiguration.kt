package io.holunda.polyflow.taskpool.collector

import io.holunda.polyflow.taskpool.collector.task.enricher.ProcessVariablesFilter
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Configures fallback if no process variable filter is defined.
 */
@AutoConfiguration
@ConditionalOnMissingBean(ProcessVariablesFilter::class)
@Configuration
class FallbackProcessVariablesFilterConfiguration {
  /**
   * Creates the empty filter used when neither a custom nor property-configured filter is available.
   *
   * @return a filter that leaves process variables unchanged.
   */
  @Bean
  fun processVariablesFilterFallback(): ProcessVariablesFilter = ProcessVariablesFilter()
}
