package io.holunda.camunda.taskpool.urlresolver

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean


/**
 * No @Configuration required.
 * Configuration used via auto-configuration.
 */
@ConditionalOnMissingBean(TasklistUrlResolver::class)
@EnableConfigurationProperties(TasklistUrlProperties::class)
class FallbackTasklistUrlResolverConfiguration {

  /**
   * Property-based Tasklist URL resolver.
   */
  @Bean
  fun propertyBasedTasklistUrlResolver(properties: TasklistUrlProperties): TasklistUrlResolver {
    return if (properties.tasklistUrl == null) {
      throw IllegalStateException("Either set polyflow.integration.tasklist.tasklist-url property or provide own implementation of TasklistUrlResolver")
    } else {
      object : TasklistUrlResolver {
        override fun getTasklistUrl(): String = properties.tasklistUrl
      }
    }
  }
}
