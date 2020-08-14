package io.holunda.camunda.taskpool.process

import io.holunda.camunda.taskpool.TaskCollectorProperties
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration
import org.camunda.bpm.spring.boot.starter.util.SpringBootProcessEnginePlugin
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ProcessDefinitionEnginePlugin(
  private val properties: TaskCollectorProperties
) : SpringBootProcessEnginePlugin() {

  private val logger: Logger = LoggerFactory.getLogger(ProcessDefinitionEnginePlugin::class.java)

  override fun preInit(springConfiguration: SpringProcessEngineConfiguration) {
    if (properties.process.enabled) {
      logger.info("EVENTING-010: Process definition registration plugin registered.")
      springConfiguration.customPostBPMNParseListeners.add(
        RegisterProcessDefinitionParseListener(springConfiguration)
      )
    } else {
      logger.info("EVENTING-011: Process definition disabled by property.")
    }
  }
}
