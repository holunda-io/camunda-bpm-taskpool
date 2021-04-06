package io.holunda.camunda.taskpool.core.process

import io.holunda.camunda.taskpool.api.process.variable.*
import io.holunda.camunda.taskpool.api.task.ProcessReference
import io.holunda.camunda.taskpool.core.EnableTaskPool
import org.assertj.core.api.Assertions
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
import java.util.*

/**
 * This test makes sure that the process variable update handler behaves correctly:
 * - if task doesn't exist, create it and handle the update command
 * - if it does exist just load and handle the update command
 */
@RunWith(SpringRunner::class)
@SpringBootTest
@ActiveProfiles("itest")
class ProcessVariableAggregateITest {

  @Autowired
  private lateinit var commandGateway: CommandGateway

  @Autowired
  private lateinit var eventBus: EventBus

  private val receivedEvents: MutableList<EventMessage<*>> = mutableListOf()
  private val processReference = ProcessReference(
    definitionKey = "process_key",
    instanceId = "0815",
    executionId = "12345",
    definitionId = "76543",
    name = "My process",
    applicationName = "myExample"
  )

  @Before
  fun registerHandler() {
    eventBus.subscribe { messages -> receivedEvents.addAll(messages) }
  }

  @Test
  fun `should accept update after create`() {

    val variableInstanceId = UUID.randomUUID().toString()

    val create = CreateProcessVariableCommand(
      variableInstanceId = variableInstanceId,
      variableName = "name",
      sourceReference = processReference,
      scopeActivityInstanceId = UUID.randomUUID().toString(),
      revision = 1,
      value = PrimitiveProcessVariableValue(4711)
    )

    val update = UpdateProcessVariableCommand(
      variableInstanceId = variableInstanceId,
      variableName = "name",
      sourceReference = processReference,
      scopeActivityInstanceId = UUID.randomUUID().toString(),
      revision = 1,
      value = PrimitiveProcessVariableValue(4712)
    )

    commandGateway.sendAndWait<Void>(create)
    commandGateway.sendAndWait<Void>(update)

    assertThat(receivedEvents.size).isEqualTo(2)
    assertThat((receivedEvents[0].payload as ProcessVariableCreatedEvent).value).isEqualTo(PrimitiveProcessVariableValue(4711))
    assertThat((receivedEvents[1].payload as ProcessVariableUpdatedEvent).value).isEqualTo(PrimitiveProcessVariableValue(4712))
  }

  @Test
  fun `should accept update without create`() {

    val variableInstanceId = UUID.randomUUID().toString()

    val update = UpdateProcessVariableCommand(
      variableInstanceId = variableInstanceId,
      variableName = "name",
      sourceReference = processReference,
      scopeActivityInstanceId = UUID.randomUUID().toString(),
      revision = 1,
      value = PrimitiveProcessVariableValue(4711)
    )

    commandGateway.sendAndWait<Void>(update)
    commandGateway.sendAndWait<Void>(update.copy(value = PrimitiveProcessVariableValue(4712)))

    assertThat(receivedEvents.size).isEqualTo(2)
    assertThat((receivedEvents[0].payload as ProcessVariableUpdatedEvent).value).isEqualTo(PrimitiveProcessVariableValue(4711))
    assertThat((receivedEvents[1].payload as ProcessVariableUpdatedEvent).value).isEqualTo(PrimitiveProcessVariableValue(4712))
  }


  @SpringBootApplication
  @EnableTaskPool
  class TestApplication

}
