package io.holunda.polyflow.taskpool.collector.process.definition

import io.github.oshai.kotlinlogging.KotlinLogging
import io.holunda.polyflow.taskpool.collector.CamundaTaskpoolCollectorProperties
import io.holunda.polyflow.taskpool.collector.ProcessDefinitionEnricherType
import io.holunda.polyflow.taskpool.sender.process.definition.ProcessDefinitionCommandSender
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import org.camunda.bpm.spring.boot.starter.util.SpringBootProcessEnginePlugin
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

private val logger = KotlinLogging.logger {}

/**
 * Configuration for collecting the process definitions and sending them to taskpool core as commands.
 */
@Configuration
@ConditionalOnProperty(value = ["polyflow.integration.collector.camunda.process-definition.enabled"], havingValue = "true", matchIfMissing = false)
class ProcessDefinitionCollectorConfiguration(
  private val camundaTaskpoolCollectorProperties: CamundaTaskpoolCollectorProperties
) {

  /**
   * Registers a plugin that is got refreshed on parse of BPMN.
   */
  @Bean
  fun processDefinitionEnginePlugin(applicationEventPublisher: ApplicationEventPublisher) = object : SpringBootProcessEnginePlugin() {
    override fun preInit(processEngineConfiguration: ProcessEngineConfigurationImpl) {
      if (camundaTaskpoolCollectorProperties.processDefinition.enabled) {
        logger.info { "EVENTING-010: Process definition registration plugin activated." }

        processEngineConfiguration.customPostBPMNParseListeners.add(
          RefreshProcessDefinitionRegistrationParseListener(processEngineConfiguration)
        )
        processEngineConfiguration.customPreCommandInterceptorsTxRequired =
          (processEngineConfiguration.customPreCommandInterceptorsTxRequired ?: emptyList()) +
            ProcessDefinitionLifecycleCommandInterceptor(applicationEventPublisher)
      } else {
        logger.info { "EVENTING-011: Process definition registration disabled by property." }
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
   * Creates the built-in process-definition enricher selected by properties.
   */
  @Bean
  @ConditionalOnExpression("'\${polyflow.integration.collector.camunda.process-definition.enricher.type:no}' != 'custom'")
  fun processDefinitionCommandEnricher(): ProcessDefinitionCommandEnricher =
    when (camundaTaskpoolCollectorProperties.processDefinition.enricher.type) {
      ProcessDefinitionEnricherType.property -> PropertyBasedProcessDefinitionEnricher(camundaTaskpoolCollectorProperties.processDefinition.enricher.attributes)
      ProcessDefinitionEnricherType.no -> EmptyProcessDefinitionCommandEnricher()
      ProcessDefinitionEnricherType.custom -> error("Custom process-definition enrichment requires an application ProcessDefinitionCommandEnricher bean.")
    }

  /**
   * Creates the command processor and applies definition enrichment before sending.
   */
  @Bean
  fun processDefinitionProcessor(
    processDefinitionCommandSender: ProcessDefinitionCommandSender,
    processDefinitionCommandEnricher: ProcessDefinitionCommandEnricher,
  ) = ProcessDefinitionProcessor(
    processDefinitionCommandSender = processDefinitionCommandSender,
    enricher = processDefinitionCommandEnricher,
  )
}
