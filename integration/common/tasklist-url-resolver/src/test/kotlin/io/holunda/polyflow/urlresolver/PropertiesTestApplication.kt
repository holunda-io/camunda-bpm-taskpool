package io.holunda.polyflow.urlresolver

import org.mockito.kotlin.mock
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

@SpringBootApplication
@EnableConfigurationProperties(TasklistUrlProperties::class)
class PropertiesTestApplication {

  @Bean
  fun ownResolver(): TasklistUrlResolver = mock()

}
