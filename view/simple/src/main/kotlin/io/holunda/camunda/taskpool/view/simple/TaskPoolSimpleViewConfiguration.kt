package io.holunda.camunda.taskpool.view.simple

import io.holunda.camunda.taskpool.view.simple.service.TaskPoolService
import mu.KLogging
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import javax.annotation.PostConstruct

@ComponentScan
@Configuration
open class TaskPoolSimpleViewConfiguration {

  companion object: KLogging()

  @Bean
  @ConditionalOnProperty(prefix = "camunda.taskpool.view.simple", name = ["replay"], matchIfMissing = true)
  open fun initializeSimpleView(taskPoolService: TaskPoolService) = ApplicationRunner {
    taskPoolService.restore()
  }

  @PostConstruct
  open fun info() {
    logger.info { "VIEW-SIMPLE-001: Initialized simple view" }
  }
}
