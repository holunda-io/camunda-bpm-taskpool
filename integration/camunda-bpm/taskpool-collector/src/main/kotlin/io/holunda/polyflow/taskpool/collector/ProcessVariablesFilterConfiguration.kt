package io.holunda.polyflow.taskpool.collector

import io.github.oshai.kotlinlogging.KotlinLogging
import io.holunda.polyflow.taskpool.collector.task.enricher.ProcessVariablesFilter
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean

private val logger = KotlinLogging.logger {}

/**
 * Configures a [ProcessVariablesFilter] from application properties.
 */
@AutoConfigureBefore(FallbackProcessVariablesFilterConfiguration::class)
@AutoConfigureAfter(CamundaTaskpoolCollectorConfiguration::class)
@ConditionalOnProperty(
  prefix = "polyflow.integration.collector.camunda.task.enricher.process-variables-filter",
  name = ["enabled"],
  havingValue = "true"
)
@ConditionalOnMissingBean(ProcessVariablesFilter::class)
class ProcessVariablesFilterConfiguration {

  @Bean
  fun processVariablesFilter(properties: CamundaTaskpoolCollectorProperties): ProcessVariablesFilter =
    ProcessVariablesFilter(*properties.task.enricher.processVariablesFilter.toVariableFilters())
      .also {
        logger.info { "COLLECTOR-017: Process Variable Filters configured via ${properties.task.enricher.processVariablesFilter.filters.size} properties." }
      }
}
