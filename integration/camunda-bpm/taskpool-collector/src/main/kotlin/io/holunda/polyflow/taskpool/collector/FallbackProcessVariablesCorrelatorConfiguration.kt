package io.holunda.polyflow.taskpool.collector

import io.holunda.polyflow.taskpool.collector.task.enricher.ProcessVariablesCorrelator
import org.camunda.bpm.spring.boot.starter.CamundaBpmAutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Configures fallback if no process variable correlator is defined.
 * No @Configuration required.
 * Configuration used via auto-configuration.
 */
@ConditionalOnMissingBean(ProcessVariablesCorrelator::class)
class FallbackProcessVariablesCorrelatorConfiguration {
  /**
   * Empty correlator.
   */
  @Bean
  fun processVariablesCorrelatorFallback(): ProcessVariablesCorrelator = ProcessVariablesCorrelator()
}
