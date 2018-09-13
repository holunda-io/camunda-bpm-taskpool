package io.holunda.camunda.taskpool

import io.holunda.camunda.taskpool.enricher.*
import mu.KLogging
import org.camunda.bpm.engine.RuntimeService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan
@EnableConfigurationProperties(TaskCollectorProperties::class)
open class TaskCollectorConfiguration(
  private val properties: TaskCollectorProperties,
  private val runtimeService: RuntimeService
) {

  companion object : KLogging()

  @Bean
  @ConditionalOnExpression("'\${camunda.taskpool.collector.enricher.type}' != 'custom'")
  open fun createEnricher(): CreateCommandEnricher? =
    when (properties.enricher.type) {
      TaskCollectorEnricherType.processVariables.name -> ProcessVariablesCreateCommandEnricher(runtimeService)
      TaskCollectorEnricherType.no.name -> EmptyCreateCommandEnricher()
      else -> null
    }

  @Bean
  @ConditionalOnExpression("'\${camunda.taskpool.collector.enricher.type}' != 'custom'")
  open fun assignEnricher(): AssignCommandEnricher? =
    when (properties.enricher.type) {
      TaskCollectorEnricherType.processVariables.name -> ProcessVariablesAssignCommandEnricher(runtimeService)
      TaskCollectorEnricherType.no.name -> EmptyAssignCommandEnricher()
      else -> null
    }

  @Bean
  @ConditionalOnExpression("'\${camunda.taskpool.collector.enricher.type}' != 'custom'")
  open fun deleteEnricher(): DeleteCommandEnricher? =
    when (properties.enricher.type) {
      TaskCollectorEnricherType.processVariables.name -> ProcessVariablesDeleteCommandEnricher(runtimeService)
      TaskCollectorEnricherType.no.name -> EmptyDeleteCommandEnricher()
      else -> null
    }

  @Bean
  @ConditionalOnExpression("'\${camunda.taskpool.collector.enricher.type}' != 'custom'")
  open fun completeEnricher(): CompleteCommandEnricher? =
    when (properties.enricher.type) {
      TaskCollectorEnricherType.processVariables.name -> ProcessVariablesCompleteCommandEnricher(runtimeService)
      TaskCollectorEnricherType.no.name -> EmptyCompleteCommandEnricher()
      else -> null
    }

  @Autowired
  open fun reportConfiguration() {
    when (properties.enricher.type) {
      TaskCollectorEnricherType.processVariables.name -> logger.info { "ENRICHER-001: Camunda Taskpool commands will be enriched with process variables." }
      TaskCollectorEnricherType.no.name -> logger.info { "ENRICHER-002: Camunda Taskpool commands will not be enriched." }
      else -> logger.info { "ENRICHER-003: Camunda Taskpool commands will not be enriched by a custom enricher." }
    }
    if (properties.sender.enabled) {
      logger.info { "SENDER-001: Camunda Taskpool commands will be distributed over command bus." }
    } else {
      logger.info { "SENDER-002: Camunda Taskpool command distribution is disabled by property." }
    }
  }

}
