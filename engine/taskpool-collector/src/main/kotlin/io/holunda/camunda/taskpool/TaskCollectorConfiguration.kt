package io.holunda.camunda.taskpool

import io.holunda.camunda.taskpool.enricher.*
import io.holunda.camunda.taskpool.sender.CommandSender
import io.holunda.camunda.taskpool.sender.TxAwareAccumulatingCommandSender
import io.holunda.camunda.taskpool.sender.accumulator.CommandAccumulator
import io.holunda.camunda.taskpool.sender.accumulator.ProjectingCommandAccumulator
import io.holunda.camunda.taskpool.sender.gateway.CommandListGateway
import io.holunda.camunda.taskpool.urlresolver.TasklistUrlResolver
import org.camunda.bpm.engine.RuntimeService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import javax.annotation.PostConstruct

@Configuration
@ComponentScan
@EnableConfigurationProperties(TaskCollectorProperties::class)
open class TaskCollectorConfiguration(
  private val properties: TaskCollectorProperties,
  private val runtimeService: RuntimeService,
  private val filter: ProcessVariablesFilter,
  private val correlator: ProcessVariablesCorrelator
) {

  private val logger: Logger = LoggerFactory.getLogger(TaskCollectorConfiguration::class.java)

  /**
   * Create accumulator.
   */
  @Bean
  open fun commandAccumulator(): CommandAccumulator = ProjectingCommandAccumulator()

  /**
   * Create enricher.
   */
  @Bean
  @ConditionalOnExpression("'\${camunda.taskpool.collector.enricher.type}' != 'custom'")
  open fun processVariablesEnricher(): VariablesEnricher =
    when (properties.enricher.type) {
      TaskCollectorEnricherType.processVariables -> ProcessVariablesTaskCommandEnricher(runtimeService, filter, correlator)
      TaskCollectorEnricherType.no -> EmptyTaskCommandEnricher()
      else -> throw IllegalStateException("Could not initialize enricher, used ${properties.enricher.type} type.")
    }

  /**
   * Create command sender.
   */
  @Bean
  @ConditionalOnExpression("'\${camunda.taskpool.collector.sender.type}' != 'custom'")
  open fun txCommandSender(commandListGateway: CommandListGateway, accumulator: CommandAccumulator): CommandSender =
    when (properties.sender.type) {
      TaskSenderType.tx -> TxAwareAccumulatingCommandSender(commandListGateway, accumulator)
      else -> throw IllegalStateException("Could not initialize sender, used ${properties.sender.type} type.")
    }


  @PostConstruct
  open fun printSenderConfiguration() {
    if (properties.sender.enabled) {
      logger.info("SENDER-001: Camunda Taskpool commands will be distributed over command bus.")
    } else {
      logger.info("SENDER-002: Camunda Taskpool command distribution is disabled by property.")
    }
  }

  @PostConstruct
  open fun printEnricherConfiguration() {
    when (properties.enricher.type) {
      TaskCollectorEnricherType.processVariables -> logger.info("ENRICHER-001: Camunda Taskpool commands will be enriched with process variables.")
      TaskCollectorEnricherType.no -> logger.info("ENRICHER-002: Camunda Taskpool commands will not be enriched.")
      else -> logger.info("ENRICHER-003: Camunda Taskpool commands will not be enriched by a custom enricher.")
    }
  }
}

