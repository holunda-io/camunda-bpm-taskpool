package io.holunda.polyflow.taskpool.core.task

import io.holunda.camunda.taskpool.api.business.addCorrelation
import io.holunda.camunda.taskpool.api.business.newCorrelations
import io.holunda.camunda.taskpool.api.task.*
import io.holunda.polyflow.taskpool.core.TestApplication
import org.assertj.core.api.Assertions.assertThat
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.eventhandling.EventBus
import org.axonframework.eventhandling.EventMessage
import org.axonframework.messaging.unitofwork.UnitOfWork
import org.camunda.bpm.engine.variable.Variables
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.time.Instant
import java.util.*

/**
 * This test makes sure that the task create handler behaves correctly:
 * - if task doesn't exist, create it and handle the create command
 * - if it does exist (e.g. we run populate), just handle the create command
 */
@SpringBootTest(classes = [TestApplication::class])
@ActiveProfiles("itest")
internal class TaskHandlerAggregateITest {

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
  private val taskId = UUID.randomUUID().toString()
  private val now = Date.from(Instant.now())

  val createCommand = CreateTaskCommand(
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


  @BeforeEach
  fun registerHandler() {
    eventBus.subscribe { messages -> receivedEvents.addAll(messages) }
  }

  @Test
  fun `should accept second create task command for the same task id`() {
    commandGateway.sendAndWait<String>(createCommand)
    commandGateway.sendAndWait<String>(createCommand.copy(description = "Changed value"))

    assertThat(receivedEvents.size).isEqualTo(2)
    assertThat((receivedEvents[1].payload as TaskCreatedEngineEvent).description).isEqualTo("Changed value")
  }

  @Test
  fun `should accept batch command`() {
    val addCandidateUsersCommand = AddCandidateUsersCommand(id = createCommand.id, candidateUsers = setOf("kermit"))
    val addCandidateUsersGroups = AddCandidateGroupsCommand(id = createCommand.id, candidateGroups = setOf("muppets"))
    commandGateway.sendAndWait<String>(
      BatchCommand(id = createCommand.id, commands = listOf(
        createCommand,
        addCandidateUsersCommand,
        addCandidateUsersGroups
      ))
    )
    assertThat(receivedEvents.size).isEqualTo(3)
    assertThat(receivedEvents[0].payload).isInstanceOf(TaskCreatedEngineEvent::class.java)
    assertThat(receivedEvents[1].payload).isInstanceOf(TaskCandidateUserChanged::class.java)
    assertThat(receivedEvents[2].payload).isInstanceOf(TaskCandidateGroupChanged::class.java)
  }

  @AfterEach
  fun clean() {
    receivedEvents.clear()
  }
}
