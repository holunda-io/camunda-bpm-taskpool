package io.holunda.camunda.taskpool.view.simple

import io.holunda.camunda.taskpool.view.simple.service.SimpleServiceViewProcessingGroup
import mu.KLogging
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
   * Logs a little.
   */
  @PostConstruct
  fun info() {
    logger.info { "VIEW-SIMPLE-001: Initialized simple view" }
  }
}
