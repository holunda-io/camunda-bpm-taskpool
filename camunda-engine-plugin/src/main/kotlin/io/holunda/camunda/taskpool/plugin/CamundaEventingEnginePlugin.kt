package io.holunda.camunda.taskpool.plugin

import mu.KLogging
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration
import org.camunda.bpm.spring.boot.starter.util.SpringBootProcessEnginePlugin
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import org.camunda.bpm.engine.impl.history.handler.CompositeDbHistoryEventHandler



@Component
open class CamundaEventingEnginePlugin(
  private val publisher: ApplicationEventPublisher,
  private val properties: CamundaEventingProperties
) : SpringBootProcessEnginePlugin() {

  companion object : KLogging()

  override fun preInit(processEngineConfiguration: SpringProcessEngineConfiguration) {
    if (properties.enabled) {

      logger.info("EVENTING-001: Initialized Camunda Eventing Engine Plugin.")
      if (properties.taskEventing) {
        logger.info { "EVENTING-003: Task events are will be published as Spring Events." }
      } else {
        logger.info { "EVENTING-004: Task eventing is disabled." }
      }
      if (properties.executionEventing) {
        logger.info { "EVENTING-005: Execution events are will be published as Spring Events." }
      } else {
        logger.info { "EVENTING-006: Execution eventing is disabled." }
      }
      processEngineConfiguration.customPostBPMNParseListeners.add(PublishDelegateParseListener(this.publisher, this.properties))

      if (properties.historicEventing) {
        logger.info { "EVENTING-007: History events will be published as Spring events." }
        processEngineConfiguration.historyEventHandler = CompositeDbHistoryEventHandler(PublishHistoryEventHandler(this.publisher))
      } else {
        logger.info { "EVENTING-008: History eventing is disabled." }
      }

    } else {
      logger.info { "EVENTING-002: Camunda Eventing Plugin is found, but disabled." }
    }
  }

}
