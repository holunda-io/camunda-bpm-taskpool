package io.holunda.camunda.taskpool.urlresolver

import io.holunda.camunda.taskpool.view.TaskUrlResolver
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(TaskUrlResolverProperties::class)
open class PropertyBasedTaskUrlResolverConfiguration {

  @Bean
  @ConditionalOnMissingBean(TaskUrlResolver::class)
  open fun taskUrlResolver(props: TaskUrlResolverProperties) = PropertyBasedTaskUrlResolver(props)
}
