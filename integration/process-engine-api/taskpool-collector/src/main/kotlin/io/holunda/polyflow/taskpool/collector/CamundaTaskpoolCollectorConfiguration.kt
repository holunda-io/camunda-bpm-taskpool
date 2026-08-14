package io.holunda.polyflow.taskpool.collector

import io.github.oshai.kotlinlogging.KotlinLogging
import io.holunda.polyflow.spring.ApplicationNameBeanPostProcessor
import io.holunda.polyflow.taskpool.collector.task.TaskCollectorConfiguration
import jakarta.annotation.PostConstruct
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import

private val logger = KotlinLogging.logger {}

/**
 * Configuration of collector.
 */
@ComponentScan(
  basePackageClasses = [
    TaskCollectorConfiguration::class
  ]
)
@EnableConfigurationProperties(CamundaTaskpoolCollectorProperties::class)
@Import(ApplicationNameBeanPostProcessor::class)
class CamundaTaskpoolCollectorConfiguration(
  private val properties: CamundaTaskpoolCollectorProperties
) {

  /**
   * Prints sender config.
   */
  @PostConstruct
  fun printConfiguration() {
    if (properties.task.enabled) {
      logger.info { "COLLECTOR-001: Task commands will be collected." }
      when (properties.task.enricher.type) {
        TaskCollectorEnricherType.processVariables -> logger.info { "ENRICHER-001: Task commands will be enriched with process variables." }
        TaskCollectorEnricherType.no -> logger.info { "ENRICHER-002: Task commands will not be enriched." }
        else -> logger.info { "ENRICHER-003: Task commands will be enriched by a custom enricher." }
      }
    } else {
      logger.info { "COLLECTOR-002: Task commands won't be collected." }
    }
  }
}

