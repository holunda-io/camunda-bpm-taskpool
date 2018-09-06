package io.holunda.camunda.taskpool.plugin

import mu.KLogging
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration
import org.camunda.bpm.spring.boot.starter.util.SpringBootProcessEnginePlugin
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
open class CamundaEventingEnginePlugin(
  private val publisher: ApplicationEventPublisher,
  private val properties: CamundaEventingProperties
) : SpringBootProcessEnginePlugin() {

  companion object : KLogging()

  override fun preInit(processEngineConfiguration: SpringProcessEngineConfiguration) {
    if (properties.enabled) {
      logger.info("EVENTING-001: Initialized Camunda Eventing Engine Plugin. All Camunda events are now available as Spring events.")
      processEngineConfiguration.customPostBPMNParseListeners.add(PublishDelegateParseListener(this.publisher))
    } else {
      logger.info { "EVENTING-002: Camunda Eventing Plugin is found, but disabled." }
    }
  }

}
