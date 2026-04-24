package io.holunda.polyflow.taskpool.collector.process.instance

import io.holunda.polyflow.taskpool.collector.CamundaTaskpoolCollectorProperties
import io.holunda.polyflow.taskpool.sender.process.instance.ProcessInstanceCommandSender
import org.camunda.bpm.engine.RepositoryService
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
   * Constructs processor, sending the commands to the sender.
   */
  @Bean
  fun processInstanceProcessor(processInstanceCommandSender: ProcessInstanceCommandSender) = ProcessInstanceProcessor(
    processInstanceCommandSender = processInstanceCommandSender,
  )
}
