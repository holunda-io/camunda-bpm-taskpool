package io.holunda.camunda.taskpool.itest

import org.mockito.kotlin.mock
import io.holunda.camunda.taskpool.EnableCamundaTaskpoolCollector
import io.holunda.camunda.taskpool.sender.gateway.CommandListGateway
import org.axonframework.commandhandling.gateway.CommandGateway
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication
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
