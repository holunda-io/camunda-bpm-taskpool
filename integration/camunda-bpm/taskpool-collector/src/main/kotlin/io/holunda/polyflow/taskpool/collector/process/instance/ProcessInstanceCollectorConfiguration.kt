package io.holunda.polyflow.taskpool.collector.process.instance

import io.holunda.polyflow.taskpool.collector.CamundaTaskpoolCollectorProperties
import io.holunda.polyflow.taskpool.collector.TaskCollectorEnricherType
import io.holunda.polyflow.taskpool.collector.task.enricher.ProcessVariablesCorrelator
import io.holunda.polyflow.taskpool.collector.task.enricher.ProcessVariablesFilter
import io.holunda.polyflow.taskpool.sender.process.instance.ProcessInstanceCommandSender
import org.camunda.bpm.engine.HistoryService
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.RuntimeService
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Configuration of process instance collection.
 */
@Configuration
@ConditionalOnProperty(value = ["polyflow.integration.collector.camunda.process-instance.enabled"], havingValue = "true", matchIfMissing = false)
class ProcessInstanceCollectorConfiguration(
  private val camundaTaskpoolCollectorProperties: CamundaTaskpoolCollectorProperties
) {

  /**
   * Constructs collector reacting to Camunda History Events, transforming them to polyflow commands.
   */
  @Bean
  fun processInstanceEventCollectorService(repositoryService: RepositoryService) = ProcessInstanceEventCollectorService(
    camundaTaskpoolCollectorProperties = camundaTaskpoolCollectorProperties,
    repositoryService = repositoryService
  )

  /**
   * Creates the loader used by process-instance variable enrichment.
   */
  @Bean
  fun processInstanceVariableLoader(
    runtimeService: RuntimeService,
    historyService: HistoryService,
  ) = ProcessInstanceVariableLoader(runtimeService, historyService)

  /**
   * Creates the built-in process-instance enricher using the task enricher's shared filter and correlator configuration.
   */
  @Bean
  @ConditionalOnExpression("'\${polyflow.integration.collector.camunda.task.enricher.type}' != 'custom'")
  fun processInstanceVariablesEnricher(
    processInstanceVariableLoader: ProcessInstanceVariableLoader,
    filter: ProcessVariablesFilter,
    correlator: ProcessVariablesCorrelator,
  ): ProcessInstanceVariablesEnricher =
    when (camundaTaskpoolCollectorProperties.task.enricher.type) {
      TaskCollectorEnricherType.processVariables -> ProcessVariablesProcessInstanceCommandEnricher(filter, correlator, processInstanceVariableLoader)
      TaskCollectorEnricherType.no -> EmptyProcessInstanceVariablesEnricher()
      else -> throw IllegalStateException("Could not initialize process instance enricher, used unknown ${camundaTaskpoolCollectorProperties.task.enricher.type} type.")
    }

  /**
   * Constructs processor, sending the commands to the sender.
   */
  @Bean
  fun processInstanceProcessor(
    processInstanceCommandSender: ProcessInstanceCommandSender,
    processInstanceVariablesEnricher: ProcessInstanceVariablesEnricher,
  ) = ProcessInstanceProcessor(
    processInstanceCommandSender = processInstanceCommandSender,
    enricher = processInstanceVariablesEnricher,
  )
}
