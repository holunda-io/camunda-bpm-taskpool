package io.holunda.polyflow.taskpool.collector.task

import dev.bpmcrafters.processengineapi.task.TaskSubscriptionApi
import io.holunda.polyflow.taskpool.collector.ProcessEngineApiTaskpoolCollectorProperties
import io.holunda.polyflow.taskpool.collector.TaskAssignerType
import io.holunda.polyflow.taskpool.collector.TaskCollectorEnricherType
import io.holunda.polyflow.taskpool.collector.task.assigner.EmptyTaskAssigner
import io.holunda.polyflow.taskpool.collector.task.assigner.ProcessVariableChangeAssigningService
import io.holunda.polyflow.taskpool.collector.task.assigner.ProcessVariablesTaskAssigner
import io.holunda.polyflow.taskpool.collector.task.enricher.EmptyTaskCommandEnricher
import io.holunda.polyflow.taskpool.collector.task.enricher.ProcessVariablesCorrelator
import io.holunda.polyflow.taskpool.collector.task.enricher.ProcessVariablesFilter
import io.holunda.polyflow.taskpool.collector.task.enricher.ProcessVariablesTaskCommandEnricher
import io.holunda.polyflow.taskpool.sender.task.EngineTaskCommandSender
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Constructs the task collector components.
 */
@Configuration
@ConditionalOnProperty(value = ["polyflow.integration.collector.processengineapi.task.enabled"], havingValue = "true", matchIfMissing = false)
class TaskCollectorConfiguration(
  private val processEngineApiTaskpoolCollectorProperties: ProcessEngineApiTaskpoolCollectorProperties,
) {

  /**
   * Create enricher.
   */
  @Bean
  @ConditionalOnExpression("'\${polyflow.integration.collector.processengineapi.task.enricher.type}' != 'custom'")
  fun processVariablesEnricher(
    filter: ProcessVariablesFilter,
    correlator: ProcessVariablesCorrelator
  ): VariablesEnricher =
    when (processEngineApiTaskpoolCollectorProperties.task.enricher.type) {
      TaskCollectorEnricherType.processVariables -> ProcessVariablesTaskCommandEnricher(
        processVariablesFilter = filter,
        processVariablesCorrelator = correlator,
      )

      TaskCollectorEnricherType.no -> EmptyTaskCommandEnricher()
      else -> throw IllegalStateException("Could not initialize task enricher, used unknown ${processEngineApiTaskpoolCollectorProperties.task.enricher.type} type.")
    }

  /**
   * Creates an empty task assigner if no assigner is defined.
   */
  @Bean
  @ConditionalOnExpression("'\${polyflow.integration.collector.processengineapi.task.assigner.type}' != 'custom'")
  fun taskAssigner(): TaskAssigner =
    when (processEngineApiTaskpoolCollectorProperties.task.assigner.type) {
      TaskAssignerType.no -> EmptyTaskAssigner()
      TaskAssignerType.processVariables -> ProcessVariablesTaskAssigner(
        processVariableTaskAssignerMapping = processEngineApiTaskpoolCollectorProperties.task.assigner.toMapping()
      )

      else -> throw IllegalStateException("Could not initialize task assigner, used unknown ${processEngineApiTaskpoolCollectorProperties.task.assigner.type} type.")
    }

  /**
   * Service responsible for changing assignees on process variable change.
   */
  @Bean
  @ConditionalOnExpression("'\${polyflow.integration.collector.processengineapi.task.assigner.type}' == 'process-variables' && '\${polyflow.integration.collector.processengineapi.process-variable.enabled}'")
  fun processVariableChangeAssigningService(taskEventCollectorService: TaskEventCollectorService) = ProcessVariableChangeAssigningService(
    taskEventCollectorService = taskEventCollectorService,
    mapping = processEngineApiTaskpoolCollectorProperties.task.assigner.toMapping()
  )

  /**
   * Constructs the task collector service and subscribes it directly to Process Engine API user-task delivery.
   */
  @Bean(TaskEventCollectorService.NAME)
  fun taskEventCollectorService(taskSubscriptionApi: TaskSubscriptionApi, applicationEventPublisher: ApplicationEventPublisher) = TaskEventCollectorService(
    processEngineApiTaskpoolCollectorProperties = processEngineApiTaskpoolCollectorProperties,
    applicationEventPublisher = applicationEventPublisher,
    taskSubscriptionApi = taskSubscriptionApi
  )

  /**
   * Creates task command processor, responsible for enrichment of commands and passing them over to the sender.
   */
  @Bean
  fun taskCommandProcessor(
    engineTaskCommandSender: EngineTaskCommandSender,
    variablesEnricher: VariablesEnricher,
    taskAssigner: TaskAssigner
  ) = TaskCommandProcessor(
    engineTaskCommandSender = engineTaskCommandSender,
    enricher = variablesEnricher,
    taskAssigner = taskAssigner
  )

}
