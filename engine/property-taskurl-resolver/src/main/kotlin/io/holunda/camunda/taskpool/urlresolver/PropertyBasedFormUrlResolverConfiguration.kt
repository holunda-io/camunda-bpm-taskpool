package io.holunda.camunda.taskpool.urlresolver

import io.holunda.camunda.taskpool.view.FormUrlResolver
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Configuration for property-based form url resolver component.
 */
@Configuration
@EnableConfigurationProperties(FormUrlResolverProperties::class)
class PropertyBasedFormUrlResolverConfiguration {

  /**
   * Constructs a resolver based on configuration properties.
   */
  @Bean
  @ConditionalOnMissingBean(FormUrlResolver::class)
  fun taskUrlResolver(props: FormUrlResolverProperties) = PropertyBasedFormUrlResolver(props)
}
