package io.holunda.polyflow.taskpool.core.process

import io.holunda.camunda.taskpool.api.process.definition.ProcessDefinitionRegisteredEvent
import io.holunda.camunda.taskpool.api.process.definition.RegisterProcessDefinitionCommand
import io.holunda.polyflow.taskpool.core.EnablePolyflowTaskPool
import org.assertj.core.api.Assertions.assertThat
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.eventhandling.EventBus
import org.axonframework.eventhandling.EventMessage
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

/**
 * This test makes sure that the process definition handler behaves correctly:
 * - if task doesn't exist, create it and handle the register command
 * - if it does exist (e.g. we run populate), just handle the create command
 */
@RunWith(SpringRunner::class)
@SpringBootTest
@ActiveProfiles("itest")
class ProcessDefinitionHandlerAggregateITest {

  @Autowired
  private lateinit var commandGateway: CommandGateway

  @Autowired
  private lateinit var eventBus: EventBus

  private val receivedEvents: MutableList<EventMessage<*>> = mutableListOf()

  @Before
  fun registerHandler() {
    eventBus.subscribe { messages -> receivedEvents.addAll(messages) }
  }

  @Test
  fun `should accept second register command for the same process definition id`() {

    val version = 1
    val command = RegisterProcessDefinitionCommand(
      "id:${version}",
      "id",
      version,
      "my application",
      "my process",
      "MVP",
      "This is a very nice process",
      "startForm",
      true,
      setOf("kermit"),
      setOf("muppetshow")
    )


    commandGateway.sendAndWait<String>(command)
    commandGateway.sendAndWait<String>(command.copy(processName = "new name"))

    assertThat(receivedEvents.size).isEqualTo(2)
    assertThat((receivedEvents[1].payload as ProcessDefinitionRegisteredEvent).processName).isEqualTo("new name")
  }

  @SpringBootApplication
  @EnablePolyflowTaskPool
  class TestApplication
}
