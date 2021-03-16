package io.holunda.camunda.taskpool

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.holunda.camunda.taskpool.enricher.*
import io.holunda.camunda.taskpool.sender.EngineTaskCommandSender
import io.holunda.camunda.taskpool.sender.SimpleEngineTaskCommandSender
import io.holunda.camunda.taskpool.sender.TxAwareAccumulatingEngineTaskCommandSender
import io.holunda.camunda.taskpool.sender.accumulator.EngineTaskCommandAccumulator
import io.holunda.camunda.taskpool.sender.accumulator.ProjectingCommandAccumulator
import io.holunda.camunda.taskpool.sender.gateway.*
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
   * Conditional object mapper, if not defined by the user.
   */
  @Bean
  @ConditionalOnMissingBean(ObjectMapper::class)
  fun taskCollectorObjectMapper(): ObjectMapper = jacksonObjectMapper().findAndRegisterModules()

  /**
   * Create accumulator.
   */
  @Bean
  fun commandAccumulator(objectMapper: ObjectMapper): EngineTaskCommandAccumulator = ProjectingCommandAccumulator(objectMapper = objectMapper)


  /**
   * Create enricher.
   */
  @Bean
  @ConditionalOnExpression("'\${camunda.taskpool.collector.task.enricher.type}' != 'custom'")
  fun processVariablesEnricher(runtimeService: RuntimeService, filter: ProcessVariablesFilter, correlator: ProcessVariablesCorrelator): VariablesEnricher =
    when (properties.task.enricher.type) {
      TaskCollectorEnricherType.processVariables -> ProcessVariablesTaskCommandEnricher(runtimeService, filter, correlator)
      TaskCollectorEnricherType.no -> EmptyTaskCommandEnricher()
      else -> throw IllegalStateException("Could not initialize task enricher, used unknown ${properties.task.enricher.type} type.")
    }

  /**
   * Create command sender.
   */
  @Bean
  @ConditionalOnExpression("'\${camunda.taskpool.collector.task.sender.type}' != 'custom'")
  fun txCommandSender(commandListGateway: CommandListGateway, accumulator: EngineTaskCommandAccumulator): EngineTaskCommandSender =
    when (properties.task.sender.type) {
      TaskSenderType.tx -> TxAwareAccumulatingEngineTaskCommandSender(commandListGateway, accumulator, properties.task.sender.sendWithinTransaction)
      TaskSenderType.simple -> SimpleEngineTaskCommandSender(commandListGateway)
      else -> throw IllegalStateException("Could not initialize sender, used unknown  ${properties.task.sender.type} type.")
    }

  /**
   * Prints sender config.
   */
  @PostConstruct
  fun printSenderConfiguration() {

    if (properties.sendCommandsEnabled) {
      logger.info("SENDER-001: Taskpool commands will be distributed over command bus.")
    } else {
      logger.info("SENDER-002: Taskpool command distribution is disabled by property.")
    }
  }


  /**
   * Prints sender config.
   */
  @PostConstruct
  fun printTaskEnricherConfiguration() {
    require(properties.enricher == null) {
      "'camunda.taskpool.collector.enricher' property has been deprecated, please use 'camunda.taskpool.collector.task.enricher' instead."
    }

    if (properties.task.enabled) {
      logger.info ("COLLECTOR-001: Task commands will be collected.")
      when (properties.task.enricher.type) {
        TaskCollectorEnricherType.processVariables -> logger.info("ENRICHER-001: Task commands will be enriched with process variables.")
        TaskCollectorEnricherType.no -> logger.info("ENRICHER-002: Task commands will not be enriched.")
        else -> logger.info("ENRICHER-003: Task commands will be enriched by a custom enricher.")
      }
    } else {
      logger.info ("COLLECTOR-002: Task commands not be collected.")
    }
  }


    /**
   * Default logging handler.
   */
  @Bean
  fun loggingTaskCommandSuccessHandler(): CommandSuccessHandler = LoggingTaskCommandSuccessHandler(LoggerFactory.getLogger(EngineTaskCommandSender::class.java))

  /**
   * Default logging handler.
   */
  @Bean
  fun loggingTaskCommandErrorHandler(): CommandErrorHandler = LoggingTaskCommandErrorHandler(LoggerFactory.getLogger(EngineTaskCommandSender::class.java))

}

