package io.holunda.polyflow.taskpool

import io.holunda.polyflow.datapool.sender.DataEntryCommandSender
import io.holunda.polyflow.taskpool.sender.task.EngineTaskCommandSender
import org.assertj.core.api.Assertions.assertThat
import org.axonframework.commandhandling.gateway.DefaultCommandGateway
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean

@SpringBootTest
internal class TaskpoolStarterITest {

  @Test
  fun `starts taskpool engine support`(
     dataEntrySender:DataEntryCommandSender,
     engineTaskCommandSender:EngineTaskCommandSender
  ) {
    assertThat(dataEntrySender).isNotNull
    assertThat(engineTaskCommandSender).isNotNull
  }


  @SpringBootApplication
  @EnableTaskpoolEngineSupport
  @EnableProcessApplication
  class TestApplication {

    @Bean
    fun commandGateway() = DefaultCommandGateway.builder().commandBus(mock()).build()
  }
}
