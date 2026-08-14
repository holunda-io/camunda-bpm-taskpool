package io.holunda.polyflow.taskpool.collector.task

import dev.bpmcrafters.processengineapi.task.support.UserTaskSupport
import io.github.oshai.kotlinlogging.KotlinLogging
import io.holunda.camunda.taskpool.api.task.EngineTaskCommandFilter
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
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

private val logger = KotlinLogging.logger {}

/**
 * Constructs the task collector components.
 */
@Configuration
@ConditionalOnProperty(value = ["polyflow.integration.collector.processengineapi.task.enabled"], havingValue = "true", matchIfMissing = false)
class TaskCollectorConfiguration(
  private val camundaTaskpoolCollectorProperties: CamundaTaskpoolCollectorProperties,
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
    when (camundaTaskpoolCollectorProperties.task.enricher.type) {
      TaskCollectorEnricherType.processVariables -> ProcessVariablesTaskCommandEnricher(
        processVariablesFilter = filter,
        processVariablesCorrelator = correlator,
      )

      TaskCollectorEnricherType.no -> EmptyTaskCommandEnricher()
      else -> throw IllegalStateException("Could not initialize task enricher, used unknown ${camundaTaskpoolCollectorProperties.task.enricher.type} type.")
    }

  /**
   * Creates an empty task assigner if no assigner is defined.
   */
  @Bean
  @ConditionalOnExpression("'\${polyflow.integration.collector.processengineapi.task.assigner.type}' != 'custom'")
  fun taskAssigner(): TaskAssigner =
    when (camundaTaskpoolCollectorProperties.task.assigner.type) {
      TaskAssignerType.no -> EmptyTaskAssigner()
      TaskAssignerType.processVariables -> ProcessVariablesTaskAssigner(
        processVariableTaskAssignerMapping = camundaTaskpoolCollectorProperties.task.assigner.toMapping()
      )

      else -> throw IllegalStateException("Could not initialize task assigner, used unknown ${camundaTaskpoolCollectorProperties.task.assigner.type} type.")
    }

  /**
   * Service responsible for changing assignees on process variable change.
   */
  @Bean
  @ConditionalOnExpression("'\${polyflow.integration.collector.processengineapi.task.assigner.type}' == 'process-variables' && '\${polyflow.integration.collector.processengineapi.process-variable.enabled}'")
  fun processVariableChangeAssigningService(userTaskSupport: UserTaskSupport) = ProcessVariableChangeAssigningService(
    userTaskSupport = userTaskSupport,
    mapping = camundaTaskpoolCollectorProperties.task.assigner.toMapping()
  )

  /**
   * Constructs the task collector service responsible for collecting Camunda Spring events and building commands out of them.
   */
  @Bean(TaskEventCollectorService.NAME)
  fun taskEventCollectorService(userTaskSupport: UserTaskSupport, applicationEventPublisher: ApplicationEventPublisher) = TaskEventCollectorService(
    camundaTaskpoolCollectorProperties = camundaTaskpoolCollectorProperties,
    applicationEventPublisher = applicationEventPublisher,
    userTaskSupport = userTaskSupport
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
  @ConditionalOnProperty(value = ["polyflow.integration.collector.processengineapi.task.importer.enabled"], havingValue = "true", matchIfMissing = false)
  fun taskServiceCollectorService(
    userTaskSupport: UserTaskSupport,
    applicationEventPublisher: ApplicationEventPublisher,
    @Autowired(required = false) engineTaskCommandFilter: EngineTaskCommandFilter?
  ): TaskServiceCollectorService {

    if (engineTaskCommandFilter == null) {
      logger.warn { "Task importer is configured, but no task filter is provided. All tasks commands will be rejected." }
    }

    return TaskServiceCollectorService(
      userTaskSupport = userTaskSupport,
      camundaTaskpoolCollectorProperties = camundaTaskpoolCollectorProperties,
      applicationEventPublisher = applicationEventPublisher,
    )
  }
}
