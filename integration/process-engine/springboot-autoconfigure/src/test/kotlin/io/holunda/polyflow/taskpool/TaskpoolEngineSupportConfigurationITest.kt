package io.holunda.polyflow.taskpool

import io.holunda.polyflow.datapool.sender.DataEntryCommandSender
import io.holunda.polyflow.taskpool.sender.task.EngineTaskCommandSender
import org.assertj.core.api.Assertions.assertThat
import org.axonframework.commandhandling.gateway.DefaultCommandGateway
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean

@SpringBootTest
internal class TaskpoolEngineSupportConfigurationITest {

  @Test
  fun `starts taskpool engine support`(
    @Autowired dataEntrySender: DataEntryCommandSender,
    @Autowired engineTaskCommandSender: EngineTaskCommandSender
  ) {
    assertThat(dataEntrySender).isNotNull
    assertThat(engineTaskCommandSender).isNotNull
  }


  @SpringBootApplication
  @EnableTaskpoolEngineSupport
  @EnableProcessApplication
  @ImportAutoConfiguration(
    io.holunda.polyflow.taskpool.collector.CamundaTaskpoolCollectorConfiguration::class,
    io.holunda.polyflow.taskpool.sender.SenderConfiguration::class,
    io.holunda.polyflow.datapool.DataEntrySenderConfiguration::class
  )
  class TestApplication {

    @Bean
    fun commandGateway() = DefaultCommandGateway.builder().commandBus(mock()).build()
  }
}
