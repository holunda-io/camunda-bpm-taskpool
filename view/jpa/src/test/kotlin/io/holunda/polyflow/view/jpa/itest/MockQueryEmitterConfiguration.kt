package io.holunda.polyflow.view.jpa.itest

import org.axonframework.queryhandling.QueryUpdateEmitter
import org.mockito.Mockito
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.*

@Configuration
@Profile("mock-query-emitter")
class MockQueryEmitterConfiguration {

  @Bean
  fun queryUpdateEmitter(): QueryUpdateEmitter = Mockito.mock(QueryUpdateEmitter::class.java)
}
