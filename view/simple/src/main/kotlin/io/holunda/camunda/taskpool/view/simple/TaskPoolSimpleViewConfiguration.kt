package io.holunda.camunda.taskpool.view.simple

import io.holunda.camunda.taskpool.view.simple.service.TaskPoolService
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@ComponentScan
@Configuration
open class TaskPoolSimpleViewConfiguration {

  @Bean
  @ConditionalOnProperty(prefix = "camunda.taskpool.view.simple", name = ["replay"], value = ["true"], matchIfMissing = true)
  open fun initializeSimpleView(taskPoolService: TaskPoolService) = ApplicationRunner {
    taskPoolService.restore()
  }
}
