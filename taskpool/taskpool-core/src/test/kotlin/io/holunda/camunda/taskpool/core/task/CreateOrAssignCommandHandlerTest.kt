package io.holunda.camunda.taskpool.core.task

import io.holunda.camunda.taskpool.api.business.addCorrelation
import io.holunda.camunda.taskpool.api.business.newCorrelations
import io.holunda.camunda.taskpool.api.task.InitialTaskCommand
import io.holunda.camunda.taskpool.api.task.ProcessReference
import org.assertj.core.api.Assertions.assertThat
import org.axonframework.eventsourcing.EventSourcingRepository
import org.camunda.bpm.engine.variable.Variables
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import java.time.Instant
import java.util.*

class CreateOrAssignCommandHandlerTest {

  @get: Rule
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  private val processReference = ProcessReference(
    definitionKey = "process_key",
    instanceId = "0815",
    executionId = "12345",
    definitionId = "76543",
    name = "My process",
    applicationName = "myExample"
  )

  @Mock
  private lateinit var eventSourcingRepository: EventSourcingRepository<TaskAggregate>

  private lateinit var initialTaskCommand: InitialTaskCommand
  private lateinit var now: Date
  private lateinit var now2: Date
  private lateinit var createOrAssignCommandHandler: TaskCommandOrderingHandler

  @Before
  fun setUp() {
    now = Date()
    now2 = Date.from(Instant.now().plusSeconds(1000))
    initialTaskCommand = InitialTaskCommand(
      id = "4711",
      name = "Foo",
      createTime = now,
      eventName = "create",
      owner = "kermit",
      taskDefinitionKey = "foo",
      formKey = "some",
      businessKey = "business123",
      enriched = true,
      sourceReference = processReference,
      candidateUsers = listOf("kermit", "gonzo"),
      candidateGroups = listOf("muppets"),
      assignee = "kermit",
      priority = 51,
      followUpDate = now2,
      dueDate = now,
      description = "Funky task",
      payload = Variables.createVariables().putValueTyped("key", Variables.stringValue("value")),
      correlations = newCorrelations().addCorrelation("Request", "business123")
    )

    createOrAssignCommandHandler = TaskCommandOrderingHandler(eventSourcingRepository)
  }


  @Test
  fun `should create TaskCreate command`() {

    val command = createOrAssignCommandHandler.create(initialTaskCommand)

    assertThat(command.assignee).isEqualTo(initialTaskCommand.assignee)
    assertThat(command.businessKey).isEqualTo(initialTaskCommand.businessKey)
    assertThat(command.candidateGroups).isEqualTo(initialTaskCommand.candidateGroups)
    assertThat(command.candidateUsers).isEqualTo(initialTaskCommand.candidateUsers)
    assertThat(command.correlations).isEqualTo(initialTaskCommand.correlations)
    assertThat(command.createTime).isEqualTo(initialTaskCommand.createTime)
    assertThat(command.description).isEqualTo(initialTaskCommand.description)
    assertThat(command.dueDate).isEqualTo(initialTaskCommand.dueDate)
    assertThat(command.enriched).isEqualTo(initialTaskCommand.enriched)
    assertThat(command.eventName).isEqualTo(initialTaskCommand.eventName)
    assertThat(command.followUpDate).isEqualTo(initialTaskCommand.followUpDate)
    assertThat(command.formKey).isEqualTo(initialTaskCommand.formKey)
    assertThat(command.id).isEqualTo(initialTaskCommand.id)
    assertThat(command.name).isEqualTo(initialTaskCommand.name)
    assertThat(command.owner).isEqualTo(initialTaskCommand.owner)
    assertThat(command.payload).isEqualTo(initialTaskCommand.payload)
    assertThat(command.priority).isEqualTo(initialTaskCommand.priority)
    assertThat(command.sourceReference).isEqualTo(initialTaskCommand.sourceReference)
    assertThat(command.taskDefinitionKey).isEqualTo(initialTaskCommand.taskDefinitionKey)
  }

  @Test
  fun `should create TaskAssign command`() {

    val command = createOrAssignCommandHandler.assign(initialTaskCommand)

    assertThat(command.assignee).isEqualTo(initialTaskCommand.assignee)
    assertThat(command.businessKey).isEqualTo(initialTaskCommand.businessKey)
    assertThat(command.candidateGroups).isEqualTo(initialTaskCommand.candidateGroups)
    assertThat(command.candidateUsers).isEqualTo(initialTaskCommand.candidateUsers)
    assertThat(command.correlations).isEqualTo(initialTaskCommand.correlations)
    assertThat(command.createTime).isEqualTo(initialTaskCommand.createTime)
    assertThat(command.description).isEqualTo(initialTaskCommand.description)
    assertThat(command.dueDate).isEqualTo(initialTaskCommand.dueDate)
    assertThat(command.enriched).isEqualTo(initialTaskCommand.enriched)
    assertThat(command.eventName).isEqualTo(initialTaskCommand.eventName)
    assertThat(command.followUpDate).isEqualTo(initialTaskCommand.followUpDate)
    assertThat(command.id).isEqualTo(initialTaskCommand.id)
    assertThat(command.name).isEqualTo(initialTaskCommand.name)
    assertThat(command.owner).isEqualTo(initialTaskCommand.owner)
    assertThat(command.payload).isEqualTo(initialTaskCommand.payload)
    assertThat(command.priority).isEqualTo(initialTaskCommand.priority)
    assertThat(command.sourceReference).isEqualTo(initialTaskCommand.sourceReference)
    assertThat(command.taskDefinitionKey).isEqualTo(initialTaskCommand.taskDefinitionKey)
  }

  @Test
  fun `should create TaskAttributeUpdate command`() {

    val command = createOrAssignCommandHandler.update(initialTaskCommand)

    assertThat(command.assignee).isEqualTo(initialTaskCommand.assignee)
    assertThat(command.description).isEqualTo(initialTaskCommand.description)
    assertThat(command.dueDate).isEqualTo(initialTaskCommand.dueDate)
    assertThat(command.eventName).isEqualTo("attribute-update")
    assertThat(command.followUpDate).isEqualTo(initialTaskCommand.followUpDate)
    assertThat(command.id).isEqualTo(initialTaskCommand.id)
    assertThat(command.name).isEqualTo(initialTaskCommand.name)
    assertThat(command.owner).isEqualTo(initialTaskCommand.owner)
    assertThat(command.priority).isEqualTo(initialTaskCommand.priority)
    assertThat(command.sourceReference).isEqualTo(initialTaskCommand.sourceReference)
    assertThat(command.taskDefinitionKey).isEqualTo(initialTaskCommand.taskDefinitionKey)
  }

}
