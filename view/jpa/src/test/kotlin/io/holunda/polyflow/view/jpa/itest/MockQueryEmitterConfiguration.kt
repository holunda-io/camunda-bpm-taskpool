package io.holunda.polyflow.view.jpa.itest

import org.axonframework.queryhandling.QueryUpdateEmitter
import org.mockito.Mockito
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Configuration
@Profile("mock-query-emitter")
class MockQueryEmitterConfiguration {

  @Bean
  @Primary
  fun mockingQueryUpdateEmitter(): QueryUpdateEmitter = Mockito.mock(QueryUpdateEmitter::class.java)
}
