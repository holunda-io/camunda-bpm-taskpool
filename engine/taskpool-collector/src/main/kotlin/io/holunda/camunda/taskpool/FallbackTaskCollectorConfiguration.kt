package io.holunda.camunda.taskpool

import io.holunda.camunda.taskpool.enricher.ProcessVariablesCorrelator
import io.holunda.camunda.taskpool.enricher.ProcessVariablesFilter
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean

/**
 * Configuration used via auto-configuration.
 */
@ConditionalOnMissingBean(ProcessVariablesFilter::class)
open class FallbackProcessVariablesFilterConfiguration {
  @Bean
  open fun processVariablesFilterFallback(): ProcessVariablesFilter = ProcessVariablesFilter()
}

/**
 * Configuration used via auto-configuration.
 */
@ConditionalOnMissingBean(ProcessVariablesCorrelator::class)
open class FallbackProcessVariablesCorrelatorConfiguration {
  @Bean
  open fun processVariablesCorrelatorFallback(): ProcessVariablesCorrelator = ProcessVariablesCorrelator()
}


