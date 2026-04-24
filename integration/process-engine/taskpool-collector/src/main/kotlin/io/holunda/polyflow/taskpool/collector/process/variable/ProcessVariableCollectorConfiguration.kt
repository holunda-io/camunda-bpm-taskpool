package io.holunda.polyflow.taskpool.collector.process.variable

import io.holunda.polyflow.taskpool.collector.CamundaTaskpoolCollectorProperties
import io.holunda.polyflow.taskpool.collector.task.enricher.ProcessVariablesFilter
import io.holunda.polyflow.taskpool.sender.process.variable.ProcessVariableCommandSender
import org.camunda.bpm.engine.RepositoryService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Configuration of process variable collection.
 */
@Configuration
@ConditionalOnProperty(value = ["polyflow.integration.collector.camunda.process-variable.enabled"], havingValue = "true", matchIfMissing = false)
class ProcessVariableCollectorConfiguration(
  private val camundaTaskpoolCollectorProperties: CamundaTaskpoolCollectorProperties
) {

  /**
   * Constructs collector listening to Camunda History Events and transforming them into polyflow commands.
   */
  @Bean
  fun processVariableEventCollectorService(repositoryService: RepositoryService) = ProcessVariableEventCollectorService(
    collectorProperties = camundaTaskpoolCollectorProperties,
    repositoryService = repositoryService
  )

  /**
   * Constructs processor, processing the commands and sending them to the sender.
   */
  @Bean
  fun processVariableProcessor(
    processVariableCommandSender: ProcessVariableCommandSender,
    processVariablesFilter: ProcessVariablesFilter
  ) = ProcessVariableProcessor(
    processVariableCommandSender = processVariableCommandSender,
    processVariablesFilter = processVariablesFilter
  )
}
