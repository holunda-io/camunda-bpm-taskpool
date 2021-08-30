package io.holunda.polyflow.taskpool.itest

import io.holunda.polyflow.taskpool.EnableCamundaTaskpoolCollector
import org.axonframework.commandhandling.gateway.CommandGateway
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication
import org.mockito.kotlin.mock
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary


@SpringBootApplication
@EnableProcessApplication
@EnableCamundaTaskpoolCollector
class CollectorTestApplication {

  @Bean
  @Primary
  fun testAxonCommandGateway(): CommandGateway = mock()
}
