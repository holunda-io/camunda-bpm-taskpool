package io.holunda.polyflow.urlresolver

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean


/**
 * No @Configuration required.
 * Configuration used via auto-configuration.
 */
@EnableConfigurationProperties(TasklistUrlProperties::class)
class FallbackTasklistUrlResolverAutoConfiguration {

  /**
   * Property-based Tasklist URL resolver.
   */
  @Bean
  @ConditionalOnMissingBean(TasklistUrlResolver::class)
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
