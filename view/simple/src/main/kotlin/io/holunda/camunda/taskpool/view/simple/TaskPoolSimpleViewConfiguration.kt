package io.holunda.camunda.taskpool.view.simple

import io.holunda.camunda.taskpool.view.simple.service.SimpleServiceViewProcessingGroup
import mu.KLogging
import org.axonframework.config.EventProcessingConfigurer
import org.axonframework.eventhandling.tokenstore.inmemory.InMemoryTokenStore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import javax.annotation.PostConstruct

/**
 * Configuration fo in-memory taskpool view.
 */
@ComponentScan
@Configuration
class TaskPoolSimpleViewConfiguration {

  companion object : KLogging()

  /**
   * Initializes processing group and starts replay.
   */
  @Bean
  @ConditionalOnProperty(prefix = "camunda.taskpool.view.simple", name = ["replay"], matchIfMissing = true)
  fun initializeSimpleView(
    simpleServiceViewProcessingGroup: SimpleServiceViewProcessingGroup) = ApplicationRunner {
    simpleServiceViewProcessingGroup.restore()
  }

  /**
   * Configures the in-memory (simple) view to use an in-memory token store, to make sure that the
   * token and the projection are stored in the same place.
   *
   * This is required to get independent from the globally configured token store (which is JPA, Mongo, or whatever).
   */
  @Autowired
  fun configure(eventProcessingConfigurer: EventProcessingConfigurer) {
    val processorName = "in-mem-processor"
    eventProcessingConfigurer.registerTokenStore(processorName) { InMemoryTokenStore() }
    eventProcessingConfigurer.assignProcessingGroup(SimpleServiceViewProcessingGroup.PROCESSING_GROUP, processorName)
  }

  /**
   * Logs a little.
   */
  @PostConstruct
  fun info() {
    logger.info { "VIEW-SIMPLE-001: Initialized simple view" }
  }
}
