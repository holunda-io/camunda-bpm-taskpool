package io.holunda.polyflow.taskpool.collector

import io.github.oshai.kotlinlogging.KotlinLogging
import io.holunda.polyflow.taskpool.collector.task.enricher.ProcessVariablesCorrelator
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean

private val logger = KotlinLogging.logger {}

/**
 * Configures a [ProcessVariablesCorrelator] from application properties.
 */
@AutoConfigureBefore(FallbackProcessVariablesCorrelatorConfiguration::class)
@AutoConfigureAfter(CamundaTaskpoolCollectorConfiguration::class)
@ConditionalOnProperty(
  prefix = "polyflow.integration.collector.camunda.task.enricher.process-variables-correlator",
  name = ["enabled"],
  havingValue = "true"
)
@ConditionalOnMissingBean(ProcessVariablesCorrelator::class)
class ProcessVariablesCorrelatorConfiguration {

  @Bean
  fun processVariablesCorrelator(properties: CamundaTaskpoolCollectorProperties): ProcessVariablesCorrelator =
    ProcessVariablesCorrelator(*properties.task.enricher.processVariablesCorrelator.correlations.toTypedArray())
      .also {
        logger.info { "COLLECTOR-016: Process Variable Correlator configured via ${properties.task.enricher.processVariablesCorrelator.correlations.size} properties."  }
      }
}
