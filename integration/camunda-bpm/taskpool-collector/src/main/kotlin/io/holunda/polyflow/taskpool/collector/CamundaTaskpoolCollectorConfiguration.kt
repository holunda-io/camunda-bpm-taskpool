package io.holunda.polyflow.taskpool.collector

import io.holunda.polyflow.taskpool.collector.task.BuiltInPublishDelegateParseListener
import io.holunda.polyflow.taskpool.collector.task.VariablesEnricher
import io.holunda.polyflow.taskpool.collector.task.enricher.EmptyTaskCommandEnricher
import io.holunda.polyflow.taskpool.collector.task.enricher.ProcessVariablesCorrelator
import io.holunda.polyflow.taskpool.collector.task.enricher.ProcessVariablesFilter
import io.holunda.polyflow.taskpool.collector.task.enricher.ProcessVariablesTaskCommandEnricher
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.TaskService
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor
import org.camunda.bpm.engine.spring.SpringProcessEnginePlugin
import org.camunda.bpm.spring.boot.starter.property.CamundaBpmProperties
import org.camunda.bpm.spring.boot.starter.property.EventingProperty
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import javax.annotation.PostConstruct

/**
 * Configuration of collector.
 */
@ComponentScan
@EnableConfigurationProperties(CamundaTaskpoolCollectorProperties::class)
class CamundaTaskpoolCollectorConfiguration(
  private val properties: CamundaTaskpoolCollectorProperties,
  camundaBpmProperties: CamundaBpmProperties
) {

  private val logger: Logger = LoggerFactory.getLogger(CamundaTaskpoolCollectorConfiguration::class.java)
  private val eventingProperties = camundaBpmProperties.eventing


  @Bean
  fun builtInEngineListenerPlugin(publisher: ApplicationEventPublisher) = object : SpringProcessEnginePlugin() {
    override fun preInit(processEngineConfiguration: ProcessEngineConfigurationImpl) {
      if (eventingProperties.isTask) {
        throw IllegalStateException("Standard eventing of Camunda BPM Spring boot is active for tasks. Switch it off by setting camunda.eventing.task=false to use polyflow collector.")
      }
      processEngineConfiguration.customPostBPMNParseListeners.add(
        BuiltInPublishDelegateParseListener(publisher)
      )
    }
  }

  /**
   * Create enricher.
   */
  @Bean
  @ConditionalOnExpression("'\${polyflow.integration.collector.camunda.task.enricher.type}' != 'custom'")
  fun processVariablesEnricher(
    runtimeService: RuntimeService,
    taskService: TaskService,
    commandExecutor: CommandExecutor,
    filter: ProcessVariablesFilter,
    correlator: ProcessVariablesCorrelator
  ): VariablesEnricher =
    when (properties.task.enricher.type) {
      TaskCollectorEnricherType.processVariables -> ProcessVariablesTaskCommandEnricher(runtimeService, taskService, commandExecutor, filter, correlator)
      TaskCollectorEnricherType.no -> EmptyTaskCommandEnricher()
      else -> throw IllegalStateException("Could not initialize task enricher, used unknown ${properties.task.enricher.type} type.")
    }

  /**
   * Prints sender config.
   */
  @PostConstruct
  fun printTaskEnricherConfiguration() {
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

