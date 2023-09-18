package io.holunda.polyflow.taskpool.collector.task

import io.holunda.camunda.taskpool.api.task.EngineTaskCommandFilter
import io.holunda.polyflow.taskpool.collector.CamundaTaskpoolCollectorConfiguration
import io.holunda.polyflow.taskpool.collector.CamundaTaskpoolCollectorProperties
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
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.TaskService
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor
import org.camunda.bpm.engine.spring.SpringProcessEnginePlugin
import org.camunda.bpm.spring.boot.starter.property.CamundaBpmProperties
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

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
   * Task variables loader.
   */
  @Bean
  fun taskVariablesLoader(
    runtimeService: RuntimeService,
    taskService: TaskService,
    commandExecutor: CommandExecutor
  ): TaskVariableLoader = TaskVariableLoader(runtimeService, taskService, commandExecutor)

  /**
   * Create enricher.
   */
  @Bean
  @ConditionalOnExpression("'\${polyflow.integration.collector.camunda.task.enricher.type}' != 'custom'")
  fun processVariablesEnricher(
    taskVariableLoader: TaskVariableLoader,
    filter: ProcessVariablesFilter,
    correlator: ProcessVariablesCorrelator
  ): VariablesEnricher =
    when (camundaTaskpoolCollectorProperties.task.enricher.type) {
      TaskCollectorEnricherType.processVariables -> ProcessVariablesTaskCommandEnricher(
        processVariablesFilter = filter,
        processVariablesCorrelator = correlator,
        taskVariableLoader = taskVariableLoader
      )

      TaskCollectorEnricherType.no -> EmptyTaskCommandEnricher()
      else -> throw IllegalStateException("Could not initialize task enricher, used unknown ${camundaTaskpoolCollectorProperties.task.enricher.type} type.")
    }

  /**
   * Creates an empty task assigner if no assigner is defined.
   */
  @Bean
  @ConditionalOnExpression("'\${polyflow.integration.collector.camunda.task.assigner.type}' != 'custom'")
  fun taskAssigner(taskVariableLoader: TaskVariableLoader): TaskAssigner =
    when (camundaTaskpoolCollectorProperties.task.assigner.type) {
      TaskAssignerType.no -> EmptyTaskAssigner()
      TaskAssignerType.processVariables -> ProcessVariablesTaskAssigner(
        taskVariableLoader = taskVariableLoader,
        processVariableTaskAssignerMapping = camundaTaskpoolCollectorProperties.task.assigner.toMapping()
      )

      else -> throw IllegalStateException("Could not initialize task assigner, used unknown ${camundaTaskpoolCollectorProperties.task.assigner.type} type.")
    }

  /**
   * Service responsible for changing assignees on process variable change.
   */
  @Bean
  @ConditionalOnExpression("'\${polyflow.integration.collector.camunda.task.assigner.type}' == 'process-variables' && '\${polyflow.integration.collector.camunda.process-variable.enabled}'")
  fun processVariableChangeAssigningService(taskService: TaskService) = ProcessVariableChangeAssigningService(
    taskService = taskService,
    mapping = camundaTaskpoolCollectorProperties.task.assigner.toMapping()
  )

  /**
   * Constructs the task collector service responsible for collecting Camunda Spring events and building commands out of them.
   */
  @Bean(TaskEventCollectorService.NAME)
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
    variablesEnricher: VariablesEnricher,
    taskAssigner: TaskAssigner
  ) = TaskCommandProcessor(
    engineTaskCommandSender = engineTaskCommandSender,
    enricher = variablesEnricher,
    taskAssigner = taskAssigner
  )

  /**
   * Create a task collector service collecting tasks directly from the task service of the engine.
   */
  @Bean
  @ConditionalOnProperty(value = ["polyflow.integration.collector.camunda.task.importer.enabled"], havingValue = "true", matchIfMissing = false)
  fun taskServiceCollectorService(
    taskService: TaskService,
    commandExecutor: CommandExecutor,
    applicationEventPublisher: ApplicationEventPublisher,
    @Autowired(required = false) engineTaskCommandFilter: EngineTaskCommandFilter?
  ): TaskServiceCollectorService {

    if (engineTaskCommandFilter == null) {
      CamundaTaskpoolCollectorConfiguration.logger.warn { "Task importer is configured, but no task filter is provided. All tasks commands will be rejected." }
    }

    return TaskServiceCollectorService(
      taskService = taskService,
      commandExecutor = commandExecutor,
      camundaTaskpoolCollectorProperties = camundaTaskpoolCollectorProperties,
      applicationEventPublisher = applicationEventPublisher,
      engineTaskCommandFilter = engineTaskCommandFilter ?: object : EngineTaskCommandFilter {}
    )

  }
}
