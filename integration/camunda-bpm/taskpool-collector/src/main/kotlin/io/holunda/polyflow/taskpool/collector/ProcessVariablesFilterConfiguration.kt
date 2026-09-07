package io.holunda.polyflow.taskpool.collector

import io.holunda.polyflow.taskpool.collector.task.enricher.ProcessVariablesFilter
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

/** Configures a [ProcessVariablesFilter] from application properties. */
@AutoConfigureBefore(FallbackProcessVariablesFilterConfiguration::class)
@EnableConfigurationProperties(ProcessVariablesFilterProperties::class)
@ConditionalOnProperty(
  prefix = "polyflow.integration.collector.camunda.process-variables-filter",
  name = ["enabled"],
  havingValue = "true"
)
@ConditionalOnMissingBean(ProcessVariablesFilter::class)
class ProcessVariablesFilterConfiguration {

  @Bean
  fun processVariablesFilter(properties: ProcessVariablesFilterProperties): ProcessVariablesFilter =
    ProcessVariablesFilter(*properties.toVariableFilters())
}
