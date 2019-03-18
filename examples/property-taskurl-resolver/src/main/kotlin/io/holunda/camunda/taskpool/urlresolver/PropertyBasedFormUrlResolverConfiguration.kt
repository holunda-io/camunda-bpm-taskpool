package io.holunda.camunda.taskpool.urlresolver

import io.holunda.camunda.taskpool.view.FormUrlResolver
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(FormUrlResolverProperties::class)
open class PropertyBasedFormUrlResolverConfiguration {

  @Bean
  @ConditionalOnMissingBean(FormUrlResolver::class)
  open fun taskUrlResolver(props: FormUrlResolverProperties) = PropertyBasedFormUrlResolver(props)
}
