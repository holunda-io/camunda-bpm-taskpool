package io.holunda.camunda.taskpool.cockpit

import io.holunda.camunda.taskpool.cockpit.service.TaskPoolCockpitService
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@ComponentScan
@Configuration
open class TaskPoolCockpitConfiguration {

  @Bean
  open fun initializeSimpleView(taskPoolCockpitService: TaskPoolCockpitService) = ApplicationRunner {
    // taskPoolCockpitService.restore()
  }
}
