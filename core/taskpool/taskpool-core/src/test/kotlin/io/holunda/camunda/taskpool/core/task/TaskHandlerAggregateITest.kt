package io.holunda.camunda.taskpool.core.task

import io.holunda.camunda.taskpool.api.business.addCorrelation
import io.holunda.camunda.taskpool.api.business.newCorrelations
import io.holunda.camunda.taskpool.api.task.CreateTaskCommand
import io.holunda.camunda.taskpool.api.task.ProcessReference
import io.holunda.camunda.taskpool.api.task.TaskCreatedEngineEvent
import io.holunda.camunda.taskpool.core.EnableTaskPool
import org.assertj.core.api.Assertions.assertThat
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.eventhandling.EventBus
import org.axonframework.eventhandling.EventMessage
import org.camunda.bpm.engine.variable.Variables
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import java.time.Instant
import java.util.*

/**
 * This test makes sure that the task create handler behaves correctly:
 * - if task doesn't exist, create it and handle the create command
 * - if it does exist (e.g. we run populate), just handle the create command
 */
@RunWith(SpringRunner::class)
@SpringBootTest
@ActiveProfiles("itest")
class TaskHandlerAggregateITest {

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
  fun `should accept second create task command for the same task id`() {

    val taskId = UUID.randomUUID().toString()
    val now = Date.from(Instant.now())
    val command = CreateTaskCommand(
      id = taskId,
      name = "Foo",
      createTime = now,
      owner = "kermit",
      taskDefinitionKey = "foo",
      formKey = "some",
      businessKey = "business123",
      sourceReference = processReference,
      candidateUsers = setOf("kermit", "gonzo"),
      candidateGroups = setOf("muppets"),
      assignee = "kermit",
      followUpDate = now,
      dueDate = now,
      priority = 51,
      description = "Funky task",
      payload = Variables.createVariables().putValueTyped("key", Variables.stringValue("value")),
      correlations = newCorrelations().addCorrelation("Request", "business123")
    )

    val result = commandGateway.sendAndWait<String>(command)
    val result2 = commandGateway.sendAndWait<String>(command.copy(description = "Changed value"))

    assertThat(receivedEvents.size).isEqualTo(2)
    assertThat((receivedEvents[1].payload as TaskCreatedEngineEvent).description).isEqualTo("Changed value")
  }

  @SpringBootApplication
  @EnableTaskPool
  class TestApplication
}
