package io.holunda.polyflow.taskpool.collector.task

import io.holunda.polyflow.taskpool.collector.CamundaTaskpoolCollectorProperties
import io.holunda.polyflow.taskpool.collector.TaskCollectorEnricherType
import io.holunda.polyflow.taskpool.collector.task.enricher.EmptyTaskCommandEnricher
import io.holunda.polyflow.taskpool.collector.task.enricher.ProcessVariablesCorrelator
import io.holunda.polyflow.taskpool.collector.task.enricher.ProcessVariablesFilter
import io.holunda.polyflow.taskpool.collector.task.enricher.ProcessVariablesTaskCommandEnricher
import io.holunda.polyflow.taskpool.sender.task.EngineTaskCommandSender
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.TaskService
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor
import org.camunda.bpm.engine.spring.SpringProcessEnginePlugin
import org.camunda.bpm.spring.boot.starter.property.CamundaBpmProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy

/**
 * Constructs the task collector components.
 */
@Configuration
@ConditionalOnProperty(value = ["polyflow.integration.collector.camunda.task.enabled"], havingValue = "true", matchIfMissing = false)
class TaskCollectorConfiguration(
  private val camundaTaskpoolCollectorProperties: CamundaTaskpoolCollectorProperties,
  camundaBpmProperties: CamundaBpmProperties
) {

  private val eventingProperties = camundaBpmProperties.eventing

  /**
   * Build the engine plugin to install pre-built listeners.
   */
  @Bean
  fun builtInEngineListenerPlugin(publisher: ApplicationEventPublisher) = object : SpringProcessEnginePlugin() {
    override fun preInit(processEngineConfiguration: ProcessEngineConfigurationImpl) {
      if (eventingProperties.isTask) {
        throw IllegalStateException("Standard eventing of Camunda BPM Spring boot is active for tasks. Switch it off by setting camunda.bpm.eventing.task=false to use Polyflow task collector.")
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
    when (camundaTaskpoolCollectorProperties.task.enricher.type) {
      TaskCollectorEnricherType.processVariables -> ProcessVariablesTaskCommandEnricher(runtimeService, taskService, commandExecutor, filter, correlator)
      TaskCollectorEnricherType.no -> EmptyTaskCommandEnricher()
      else -> throw IllegalStateException("Could not initialize task enricher, used unknown ${camundaTaskpoolCollectorProperties.task.enricher.type} type.")
    }


  /**
   * Constructs the task collector service responsible for collecting Camunda Spring events and building commands out of them.
   */
  @Bean
  fun taskEventCollectorService(repositoryService: RepositoryService) = TaskEventCollectorService(
    camundaTaskpoolCollectorProperties = camundaTaskpoolCollectorProperties,
    repositoryService = repositoryService
  )

  /**
   * Creates task command processor, responsible for enrichment of commands and passing them over to the sender.
   */
  @Bean
  fun taskCommandProcessor(
    engineTaskCommandSender: EngineTaskCommandSender,
    variablesEnricher: VariablesEnricher
  ) = TaskCommandProcessor(
    engineTaskCommandSender = engineTaskCommandSender,
    enricher = variablesEnricher
  )
}
