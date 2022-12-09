package io.holunda.polyflow.taskpool.collector.process.definition

import io.holunda.polyflow.taskpool.collector.CamundaTaskpoolCollectorProperties
import io.holunda.polyflow.taskpool.sender.process.definition.ProcessDefinitionCommandSender
import mu.KLogging
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import org.camunda.bpm.spring.boot.starter.util.SpringBootProcessEnginePlugin
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Configuration for collecting the process definitions and sending them to taskpool core as commands.
 */
@Configuration
@ConditionalOnProperty(value = ["polyflow.integration.collector.camunda.process-definition.enabled"], havingValue = "true", matchIfMissing = false)
class ProcessDefinitionCollectorConfiguration(
  private val camundaTaskpoolCollectorProperties: CamundaTaskpoolCollectorProperties
) {

  companion object : KLogging()

  /**
   * Registers a plugin that is got refreshed on parse of BPMN.
   */
  @Bean
  fun processDefinitionEnginePlugin() = object : SpringBootProcessEnginePlugin() {
    override fun preInit(processEngineConfiguration: ProcessEngineConfigurationImpl) {
      if (camundaTaskpoolCollectorProperties.processDefinition.enabled) {
        logger.info("EVENTING-010: Process definition registration plugin activated.")

        processEngineConfiguration.customPostBPMNParseListeners.add(
          RefreshProcessDefinitionRegistrationParseListener(processEngineConfiguration)
        )
      } else {
        logger.info("EVENTING-011: Process definition registration disabled by property.")
      }
    }
  }

  /**
   * Registers a job which is produced by the plugin above.
   */
  @Bean
  fun refreshProcessDefinitionsJobHandler(
    processDefinitionService: ProcessDefinitionService,
    applicationEventPublisher: ApplicationEventPublisher
  ) = RefreshProcessDefinitionsJobHandler(
    processDefinitionService = processDefinitionService,
    applicationEventPublisher = applicationEventPublisher
  )

  /**
   * Creates a sender reacting on process definition commands emitted by the job handler as spring events and forwards them to
   * process definition sender.
   */
  @Bean
  fun processDefinitionProcessor(processDefinitionCommandSender: ProcessDefinitionCommandSender) = ProcessDefinitionProcessor(
    processDefinitionCommandSender = processDefinitionCommandSender
  )
}
