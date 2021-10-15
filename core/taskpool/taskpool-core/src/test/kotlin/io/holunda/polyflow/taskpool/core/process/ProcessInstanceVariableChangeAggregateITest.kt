package io.holunda.polyflow.taskpool.core.process

import io.holunda.camunda.taskpool.api.process.instance.ProcessInstanceStartedEvent
import io.holunda.camunda.taskpool.api.process.instance.StartProcessInstanceCommand
import io.holunda.camunda.taskpool.api.process.variable.*
import io.holunda.camunda.taskpool.api.task.ProcessReference
import io.holunda.polyflow.taskpool.core.TestApplication
import org.assertj.core.api.Assertions.assertThat
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.eventhandling.EventBus
import org.axonframework.eventhandling.EventMessage
import org.camunda.bpm.engine.variable.Variables
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
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
@SpringBootTest(classes = [TestApplication::class])
@ActiveProfiles("itest")
class ProcessInstanceVariableChangeAggregateITest {

  @Autowired
  private lateinit var commandGateway: CommandGateway

  @Autowired
  private lateinit var eventBus: EventBus

  private val receivedEvents: MutableList<EventMessage<*>> = mutableListOf()
  private lateinit var processReference: ProcessReference

  @Before
  fun registerHandler() {
    eventBus.subscribe { messages -> receivedEvents.addAll(messages) }
    processReference = ProcessReference(
      definitionKey = "process_key",
      instanceId = UUID.randomUUID().toString(),
      executionId = "12345",
      definitionId = "76543",
      name = "My process",
      applicationName = "myExample"
    )
  }

  @Test
  fun `should accept update after create`() {

    val create = StartProcessInstanceCommand(
      processReference.instanceId,
      processReference,
      "businessKey-4711",
      "kermit",
      null,
      "start_event"
    )

    val update = ChangeProcessVariablesForExecutionCommand(
      sourceReference = processReference,
      variableChanges = listOf(
        ProcessVariableCreate(
          UUID.randomUUID().toString(),
          "var1",
          revision = 1,
          scopeActivityInstanceId = UUID.randomUUID().toString(),
          value = ObjectProcessVariableValue(Variables.createVariables().apply { putAll(mapOf("prop1" to "value1", "prop2" to 67)) })
        ),
        ProcessVariableCreate(
          UUID.randomUUID().toString(),
          "var2",
          revision = 89,
          scopeActivityInstanceId = UUID.randomUUID().toString(),
          value = PrimitiveProcessVariableValue(78)
        ),
        ProcessVariableUpdate(
          UUID.randomUUID().toString(),
          "var3",
          revision = 1,
          scopeActivityInstanceId = UUID.randomUUID().toString(),
          value = PrimitiveProcessVariableValue(78)
        )
      )
    )

    commandGateway.sendAndWait<Void>(create)
    commandGateway.sendAndWait<Void>(update)

    assertThat(receivedEvents.size).isEqualTo(2)
    assertThat(receivedEvents[0].payload).isInstanceOf(ProcessInstanceStartedEvent::class.java)
    assertThat(receivedEvents[1].payload).isInstanceOf(ProcessVariablesChangedEvent::class.java)
    assertThat((receivedEvents[1].payload as ProcessVariablesChangedEvent).variableChanges).isEqualTo(update.variableChanges)
  }

  @Test
  fun `should accept update without create`() {

    val update = ChangeProcessVariablesForExecutionCommand(
      sourceReference = processReference,
      variableChanges = listOf(
        ProcessVariableCreate(
          UUID.randomUUID().toString(),
          "var1",
          revision = 1,
          scopeActivityInstanceId = UUID.randomUUID().toString(),
          value = ObjectProcessVariableValue(Variables.createVariables().apply { putAll(mapOf("prop1" to "value1", "prop2" to 67)) })
        ),
        ProcessVariableCreate(
          UUID.randomUUID().toString(),
          "var2",
          revision = 89,
          scopeActivityInstanceId = UUID.randomUUID().toString(),
          value = PrimitiveProcessVariableValue(78)
        ),
        ProcessVariableUpdate(
          UUID.randomUUID().toString(),
          "var3",
          revision = 1,
          scopeActivityInstanceId = UUID.randomUUID().toString(),
          value = PrimitiveProcessVariableValue(78)
        )
      )
    )

    commandGateway.sendAndWait<Void>(update)

    assertThat(receivedEvents.size).isEqualTo(1)
    assertThat(receivedEvents[0].payload).isInstanceOf(ProcessVariablesChangedEvent::class.java)
    assertThat((receivedEvents[0].payload as ProcessVariablesChangedEvent).variableChanges).isEqualTo(update.variableChanges)

  }

}
