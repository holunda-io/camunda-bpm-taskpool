package io.holunda.camunda.taskpool.example.tasklist

import io.holunda.camunda.taskpool.example.tasklist.rest.mapper.ApplicationUrlLookup
import io.holunda.camunda.taskpool.example.tasklist.rest.mapper.DefaultApplicationUrlLookup
import io.holunda.camunda.taskpool.example.tasklist.rest.mapper.DefaultTaskUrlResolver
import io.holunda.camunda.taskpool.example.tasklist.rest.mapper.TaskUrlResolver
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan
@EnableConfigurationProperties(TaskUrlResolverProperties::class)
open class TasklistConfiguration {

  @Bean
  @ConditionalOnMissingBean(ApplicationUrlLookup::class)
  open fun defaultApplicationUrlLookup() = DefaultApplicationUrlLookup()

  @Bean
  @ConditionalOnMissingBean(TaskUrlResolver::class)
  open fun defaultTaskUrlResolver(lookup: ApplicationUrlLookup, props: TaskUrlResolverProperties) = DefaultTaskUrlResolver(lookup, props)
}
