package io.holunda.camunda.taskpool

import io.holunda.camunda.taskpool.enricher.*
import io.holunda.camunda.taskpool.sender.CommandSender
import io.holunda.camunda.taskpool.sender.TxAwareAccumulatingCommandSender
import io.holunda.camunda.taskpool.sender.accumulator.CommandAccumulator
import io.holunda.camunda.taskpool.sender.accumulator.ProjectingCommandAccumulator
import io.holunda.camunda.taskpool.sender.gateway.*
import org.camunda.bpm.engine.RuntimeService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import javax.annotation.PostConstruct

/**
 * Configuration of task collect.
 */
@Configuration
@ComponentScan
@EnableConfigurationProperties(TaskCollectorProperties::class)
class TaskCollectorConfiguration(
  private val properties: TaskCollectorProperties
) {

  private val logger: Logger = LoggerFactory.getLogger(TaskCollectorConfiguration::class.java)

  /**
   * Create accumulator.
   */
  @Bean
  fun commandAccumulator(): CommandAccumulator = ProjectingCommandAccumulator()


  /**
   * Create enricher.
   */
  @Bean
  @ConditionalOnExpression("'\${camunda.taskpool.collector.enricher.type}' != 'custom'")
  fun processVariablesEnricher(runtimeService: RuntimeService, filter: ProcessVariablesFilter, correlator: ProcessVariablesCorrelator): VariablesEnricher =
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
  fun txCommandSender(commandListGateway: CommandListGateway, accumulator: CommandAccumulator): CommandSender =
    when (properties.sender.type) {
      TaskSenderType.tx -> TxAwareAccumulatingCommandSender(commandListGateway, accumulator, properties.sender.sendWithinTransaction)
      else -> throw IllegalStateException("Could not initialize sender, used ${properties.sender.type} type.")
    }

  /**
   * Prints sender config.
   */
  @PostConstruct
  fun printSenderConfiguration() {
    if (properties.sender.enabled) {
      logger.info("SENDER-001: Camunda Taskpool commands will be distributed over command bus.")
    } else {
      logger.info("SENDER-002: Camunda Taskpool command distribution is disabled by property.")
    }
  }

  /**
   * Prints enricher config.
   */
  @PostConstruct
  fun printEnricherConfiguration() {
    when (properties.enricher.type) {
      TaskCollectorEnricherType.processVariables -> logger.info("ENRICHER-001: Camunda Taskpool commands will be enriched with process variables.")
      TaskCollectorEnricherType.no -> logger.info("ENRICHER-002: Camunda Taskpool commands will not be enriched.")
      else -> logger.info("ENRICHER-003: Camunda Taskpool commands will not be enriched by a custom enricher.")
    }
  }

  /**
   * Default logging handler.
   */
  @Bean
  fun loggingTaskCommandSuccessHandler(): TaskCommandSuccessHandler = LoggingTaskCommandSuccessHandler(LoggerFactory.getLogger(CommandSender::class.java))

  /**
   * Default logging handler.
   */
  @Bean
  fun loggingTaskCommandErrorHandler(): TaskCommandErrorHandler = LoggingTaskCommandErrorHandler(LoggerFactory.getLogger(CommandSender::class.java))
}

