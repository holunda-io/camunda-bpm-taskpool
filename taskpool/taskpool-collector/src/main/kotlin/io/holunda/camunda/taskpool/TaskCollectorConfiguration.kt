package io.holunda.camunda.taskpool

import io.holunda.camunda.taskpool.enricher.*
import io.holunda.camunda.taskpool.sender.CommandSender
import io.holunda.camunda.taskpool.sender.simple.SimpleCommandSender
import io.holunda.camunda.taskpool.sender.tx.TxAwareCommandSender
import org.axonframework.commandhandling.gateway.CommandGateway
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

  @Bean
  @ConditionalOnMissingBean(ProcessVariablesFilter::class)
  open fun processVariablesFilter() = ProcessVariablesFilter()

  @Bean
  @ConditionalOnMissingBean(ProcessVariablesCorrelator::class)
  open fun processVariablesCorrelator() = ProcessVariablesCorrelator()

  @Bean
  @ConditionalOnExpression("'\${camunda.taskpool.collector.enricher.type}' != 'custom'")
  open fun processVariablesEnricher(): VariablesEnricher? =
    when (properties.enricher.type) {
      TaskCollectorEnricherType.processVariables -> ProcessVariablesTaskCommandEnricher(runtimeService, filter, correlator)
      TaskCollectorEnricherType.no -> EmptyEnricher()
      else -> null
    }

  @Bean
  @ConditionalOnExpression("'\${camunda.taskpool.collector.sender.type}' != 'custom'")
  open fun simpleCommandSender(gateway: CommandGateway, enricher: VariablesEnricher): CommandSender? =
    when (properties.sender.type) {
      TaskSenderType.tx -> TxAwareCommandSender(gateway, properties, enricher)
      TaskSenderType.simple -> SimpleCommandSender(gateway, properties, enricher)
      else -> null
    }

  @PostConstruct
  open fun printEnricherConfiguration() {
    when (properties.enricher.type) {
      TaskCollectorEnricherType.processVariables -> logger.info("ENRICHER-001: Camunda Taskpool commands will be enriched with process variables.")
      TaskCollectorEnricherType.no -> logger.info("ENRICHER-002: Camunda Taskpool commands will not be enriched.")
      else -> logger.info("ENRICHER-003: Camunda Taskpool commands will not be enriched by a custom enricher.")
    }
  }

  @PostConstruct
  open fun printSenderConfiguration() {
    if (properties.sender.enabled) {
      logger.info("SENDER-001: Camunda Taskpool commands will be distributed over command bus.")
    } else {
      logger.info("SENDER-002: Camunda Taskpool command distribution is disabled by property.")
    }
  }

}
