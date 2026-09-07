package io.holunda.polyflow.taskpool.collector

import io.holunda.polyflow.taskpool.collector.task.enricher.ProcessVariablesCorrelator
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

/** Configures a [ProcessVariablesCorrelator] from application properties. */
@AutoConfigureBefore(FallbackProcessVariablesCorrelatorConfiguration::class)
@EnableConfigurationProperties(ProcessVariablesCorrelatorProperties::class)
@ConditionalOnProperty(
  prefix = "polyflow.integration.collector.camunda.process-variables-correlator",
  name = ["enabled"],
  havingValue = "true"
)
@ConditionalOnMissingBean(ProcessVariablesCorrelator::class)
class ProcessVariablesCorrelatorConfiguration {

  @Bean
  fun processVariablesCorrelator(properties: ProcessVariablesCorrelatorProperties): ProcessVariablesCorrelator =
    ProcessVariablesCorrelator(*properties.correlations.toTypedArray())
}
