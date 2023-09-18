package io.holunda.polyflow.taskpool.collector

import io.holunda.polyflow.spring.ApplicationNameBeanPostProcessor
import io.holunda.polyflow.taskpool.collector.process.definition.ProcessDefinitionCollectorConfiguration
import io.holunda.polyflow.taskpool.collector.process.instance.ProcessInstanceCollectorConfiguration
import io.holunda.polyflow.taskpool.collector.process.variable.ProcessVariableCollectorConfiguration
import io.holunda.polyflow.taskpool.collector.task.TaskCollectorConfiguration
import jakarta.annotation.PostConstruct
import mu.KLogging
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import

/**
 * Configuration of collector.
 */
@ComponentScan(
  basePackageClasses = [
    ProcessDefinitionCollectorConfiguration::class,
    ProcessInstanceCollectorConfiguration::class,
    ProcessVariableCollectorConfiguration::class,
    TaskCollectorConfiguration::class
  ]
)
@EnableConfigurationProperties(CamundaTaskpoolCollectorProperties::class)
@Import(ApplicationNameBeanPostProcessor::class)
class CamundaTaskpoolCollectorConfiguration(
  private val properties: CamundaTaskpoolCollectorProperties
) {
  companion object : KLogging()

  /**
   * Prints sender config.
   */
  @PostConstruct
  fun printConfiguration() {
    if (properties.task.enabled) {
      logger.info("COLLECTOR-001: Task commands will be collected.")
      when (properties.task.enricher.type) {
        TaskCollectorEnricherType.processVariables -> logger.info("ENRICHER-001: Task commands will be enriched with process variables.")
        TaskCollectorEnricherType.no -> logger.info("ENRICHER-002: Task commands will not be enriched.")
        else -> logger.info("ENRICHER-003: Task commands will be enriched by a custom enricher.")
      }
    } else {
      logger.info("COLLECTOR-002: Task commands won't be collected.")
    }

    if (properties.processDefinition.enabled) {
      logger.info("COLLECTOR-010: Process definition commands will be collected.")
    } else {
      logger.info("COLLECTOR-011: Process definition commands won't be collected.")
    }

    if (properties.processInstance.enabled) {
      logger.info("COLLECTOR-012: Process instance commands will be collected.")
    } else {
      logger.info("COLLECTOR-013: Process instance commands won't be collected.")
    }

    if (properties.processInstance.enabled) {
      logger.info("COLLECTOR-014: Process variable commands will be collected.")
    } else {
      logger.info("COLLECTOR-015: Process variable commands won't be collected.")
    }
  }
}

