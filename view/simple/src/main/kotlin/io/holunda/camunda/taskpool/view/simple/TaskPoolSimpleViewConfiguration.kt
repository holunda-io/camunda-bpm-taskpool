package io.holunda.camunda.taskpool.view.simple

import io.holunda.camunda.taskpool.view.simple.service.TaskPoolService
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component

@ComponentScan
@Configuration
open class TaskPoolSimpleViewConfiguration


@Component
class Initializer(
  private val taskPoolService: TaskPoolService
) : ApplicationRunner {

  override fun run(args: ApplicationArguments) {
    taskPoolService.restore()
  }
}
