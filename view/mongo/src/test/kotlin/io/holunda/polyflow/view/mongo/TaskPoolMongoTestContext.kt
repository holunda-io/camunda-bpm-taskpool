package io.holunda.polyflow.view.mongo

import com.tngtech.jgiven.integration.spring.EnableJGiven
import org.axonframework.config.EventProcessingConfiguration
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.mockito.Mockito
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
@EnableJGiven
class TaskPoolMongoTestContext {
  @Bean
  fun queryUpdateEmitter(): QueryUpdateEmitter = Mockito.mock(QueryUpdateEmitter::class.java)

  @Bean
  fun eventProcessingConfiguration(): EventProcessingConfiguration = Mockito.mock(EventProcessingConfiguration::class.java)
}
