package io.holunda.camunda.taskpool.example.tasklist

import io.holunda.camunda.taskpool.example.tasklist.rest.mapper.ApplicationUrlLookup
import io.holunda.camunda.taskpool.example.tasklist.rest.mapper.TaskUrlResolver
import io.holunda.camunda.taskpool.view.Task
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan
open class TasklistConfiguration {

  @Bean
  @ConditionalOnMissingBean(ApplicationUrlLookup::class)
  open fun defaultApplicationUrlLookup() = object : ApplicationUrlLookup {
    override fun lookup(appName: String): String = "http://localhost:8080/$appName"
  }

  @Bean
  @ConditionalOnMissingBean(TaskUrlResolver::class)
  open fun defaultTaskUrlResolver(lookup: ApplicationUrlLookup) = object : TaskUrlResolver {
    override fun resolveUrl(task: Task): String = "${lookup.lookup(task.sourceReference.applicationName)}" +
      "/${task.formKey}" +
      "?taskId=${task.id}"
  }
}
